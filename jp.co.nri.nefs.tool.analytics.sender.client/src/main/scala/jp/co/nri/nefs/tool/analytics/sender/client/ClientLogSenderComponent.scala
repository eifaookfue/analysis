package jp.co.nri.nefs.tool.analytics.sender.client

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.{BoundedMessageQueueSemantics, RequiresMessageQueue}
import akka.pattern.{gracefulStop,ask}
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.model.client.OMSAplInfo
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.util.{FileUtils, ZipCommand, ZipUtils}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.implicitConversions

trait ClientLogSenderComponent {
  self: ClientLogClassifierFactoryComponent =>
  val sender: ClientLogSender

  trait ClientLogSender {
    def start(): Unit
  }

  class DefaultClientLogSender(implicit val system: ActorSystem) extends ClientLogSender with LazyLogging {
    def start(): Unit = {
      // トップレベルActor
      val fileSenderActor = system.actorOf(Props[FileSendActor], "fileSender")
      fileSenderActor.ask(Manager.Start)(5.hours)
    }
  }

  object Manager {
    case object Start
    case object Shutdown
  }

  class ClientLogClassifierActor(clientLogClassifier: ClientLogClassifier) extends Actor with ActorLogging{
    override def receive: Receive = {
      case (line:String, lineNo: Int) =>
        log.info(s"$clientLogClassifier received #$lineNo")
        try {
          clientLogClassifier.classify(line, lineNo)
        } catch {
          case e: Exception => log.error(e,"Exception occurred in classification.")
        }
      case Manager.Shutdown =>
        clientLogClassifier.postStop()
        context stop self
      case _ =>
        log.info("received unknown message.")
    }
  }

  object ClientLogClassifierActor {
    def props(clientLogClassifier: ClientLogClassifier): Props = {
      Props(new ClientLogClassifierActor(clientLogClassifier))
    }
  }

  class FileSendActor extends Actor with RequiresMessageQueue[BoundedMessageQueueSemantics] with ActorLogging {
    final val INPUT_DIR = "inputDir"
    final val ZIP_COMMAND = "zipCommand"
    private val config = ConfigFactory.load()
    private val input = Paths.get(config.getString(INPUT_DIR))


    override def preStart(): Unit = {
      log.info("preStart called.")
    }

    override def postStop(): Unit = {
      log.info("postStop called.")
    }

    override def receive: Receive = {
      case Manager.Start =>
        val lineSenderActor = context.actorOf(FromConfig.props(Props[LineSenderActor]), "lineSender")
        // ファイル一覧の取得
        val files = FileUtils.autoClose(Files.walk(input)) {
          stream =>
            stream.iterator().asScala.toList
        }

        files.foreach(p => log.info(p.toString))
        for (file <- files) {
          if (ZipUtils.isZipFile(file)) {
            log.info(s"unzip starts $file")
            val expandedDir = ZipUtils.unzip(file)
            log.info(s"unzip ends $file")
            val files2 = FileUtils.autoClose(Files.list(expandedDir)) { s => s.iterator().asScala.map(_.toString).toList }
            for (file2 <- files2) {
              lineSenderActor ! file2
            }
            log.info(s"delete starts $file")
            FileUtils.delete(expandedDir)
            log.info(s"delete end $file")
          } else
            lineSenderActor ! file.toString
        }
        sender() ! "completed"
    }
  }

  class LineSenderActor extends Actor with RequiresMessageQueue[BoundedMessageQueueSemantics] with ActorLogging {
    final val WAIT_TIME_UNTIL_RECEIVER_ACK = "wait-time-until-receiver-ack"
    private val config = ConfigFactory.load()
    private val waitTimeUntilReceiverAck = config.getDuration(WAIT_TIME_UNTIL_RECEIVER_ACK)
    implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration = scala.concurrent.duration.Duration.fromNanos(d.toNanos)
    val CHARSETNAME: Charset = Charset.forName("MS932")
    final val ZIP_COMMAND = "zipCommand"
    val zipCommandStr: String = config.getString(ZIP_COMMAND)
    implicit val zipCommand: ZipCommand = new ZipCommand(zipCommandStr)

    override def preStart(): Unit = {
      log.info("preStart called.")
    }

    override def postStop(): Unit = {
      log.info("postStop called.")
    }

    override def receive: Receive = {
      case fileName: String =>
        val file = Paths.get(fileName)
        OMSAplInfo.valueOf(file.getFileName.toString) match {
          case Some(aplInfo) =>
            log.info(s"$file is analyzing...")
            val clientLogClassifier = clientLogClassifierFactory.create(aplInfo)
            val actor = context.actorOf(ClientLogClassifierActor.props(clientLogClassifier),
              aplInfo.fileName)
            val stream = Files.lines(file, CHARSETNAME)
            for ((line, tmpNo) <- stream.iterator().asScala.zipWithIndex) {
              val lineNo = tmpNo + 1
              log.info(s"send #$lineNo to $actor")
              actor ! (line, lineNo)
            }
            try {
              log.info("gracefulStop call starts.")
              val stopped = gracefulStop(actor, waitTimeUntilReceiverAck, Manager.Shutdown)
              Await.result(stopped, waitTimeUntilReceiverAck + 1.second)
              log.info("gracefulStop call ends.")
            } catch {
              case e: akka.pattern.AskTimeoutException =>
                log.warning("", e)
            }
            stream.close()
          case None =>
            log.debug(s"$file was not valid format, so skipped log sending.")
        }
    }
  }

}
