package jp.co.nri.nefs.tool.analytics.sender.client

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.gracefulStop
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.model.client.OMSAplInfo
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.util.{FileUtils, ZipCommand, ZipUtils}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.implicitConversions

trait ClientLogSenderComponent {
  self: ClientLogClassifierFactoryComponent =>
  val sender: ClientLogSender

  trait ClientLogSender {
    def start(): Unit
  }

  class DefaultClientLogSender(implicit val system: ActorSystem) extends ClientLogSender with LazyLogging{

    final val INPUT_DIR = "inputDir"
    final val OUT_DIR = "outDir"
    final val WAIT_TIME_UNTIL_RECEIVER_ACK = "wait-time-until-receiver-ack"
    final val ZIP_COMMAND = "zipCommand"
    private val config = ConfigFactory.load()
    private val input = Paths.get(config.getString(INPUT_DIR))
    private val waitTimeUntilReceiverAck = config.getDuration(WAIT_TIME_UNTIL_RECEIVER_ACK)
    implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration = scala.concurrent.duration.Duration.fromNanos(d.toNanos)
    val zipCommandStr: String = config.getString(ZIP_COMMAND)
    implicit val zipCommand: ZipCommand = new ZipCommand(zipCommandStr)
    val CHARSETNAME: Charset = Charset.forName("MS932")

    private def send(file: Path): Unit = {
      OMSAplInfo.valueOf(file.getFileName.toString) match {
        case Some(aplInfo) =>
          logger.info(s"$file is analyzing...")
          val clientLogClassifier = clientLogClassifierFactory.create(aplInfo)
          val actor = system.actorOf(ClientLogClassifierActor.props(clientLogClassifier),
            aplInfo.fileName)
          val stream = Files.lines(file, CHARSETNAME)
          for ((line, tmpNo) <- stream.iterator().asScala.zipWithIndex) {
            val lineNo = tmpNo + 1
            logger.info(s"send #$lineNo to $actor")
            actor ! (line, lineNo)
          }
          //actor ! PoisonPill
          try {
            logger.info("gracefulStop call starts.")
            val stopped = gracefulStop(actor, waitTimeUntilReceiverAck, Manager.Shutdown)
            Await.result(stopped, waitTimeUntilReceiverAck + 1.second)
            logger.info("gracefulStop call ends.")
          } catch {
            case e: akka.pattern.AskTimeoutException =>
              logger.warn("", e)
          }
          stream.close()
        case None =>
          logger.debug(s"$file was not valid format, so skipped log sending.")
      }
    }

    def start(): Unit = {
      // ファイル一覧の取得
      val files = FileUtils.autoClose(Files.walk(input)){
        stream =>
          stream.iterator().asScala.toList
      }

      files.foreach(p => logger.info(p.toString))
      val aggFut = Future.traverse(files) { file =>
        Future {
          if (ZipUtils.isZipFile(file)) {
            logger.info(s"unzip starts $file")
            val expandedDir = ZipUtils.unzip(file)
            logger.info(s"unzip ends $file")
            val files2 = FileUtils.autoClose(Files.list(expandedDir)) { s => s.iterator().asScala.toList }
            for (file2 <- files2) send(file2)
            logger.info(s"delete starts $file")
            FileUtils.delete(expandedDir)
            logger.info(s"delete end $file")
          } else
            send(file)
        }
      }
      Await.ready(aggFut, Duration.Inf)
      //Thread.sleep(2000)
    }
  }

  object Manager {
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

}
