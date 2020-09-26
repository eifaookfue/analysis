package jp.co.nri.nefs.tool.analytics.sender.client

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.TimeoutException

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.{BoundedMessageQueueSemantics, RequiresMessageQueue}
import akka.pattern.{ask, gracefulStop}
import akka.routing.FromConfig
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
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

  class DefaultClientLogSender(implicit val system: ActorSystem, timeout: Timeout) extends ClientLogSender with LazyLogging {
    val config: Config = ConfigFactory.load()
    final val WAIT_TIME_UNTIL_SINGLE_FILE_EXECUTION = ClientLogSenderExecutor.CONFIG_BASE + "wait-time-until-single-file-execution"
    private val waitTimeUntilSingleFileExecution = Duration.fromNanos(config.getDuration(WAIT_TIME_UNTIL_SINGLE_FILE_EXECUTION).toNanos)

    def start(): Unit = {
      // トップレベルActor
      val fileSenderActor = system.actorOf(FileSendActor.props(timeout), "fileSender")
      val fut = fileSenderActor ? Manager.Start
      logger.info("ask call starts.")
      val message =  Await.result(fut, timeout.duration)
      logger.info(s"ask call ends. received = $message")
    }
  }

  object Manager {
    case object Start
    case object Shutdown
  }

  class ClientLogClassifierActor(clientLogClassifier: ClientLogClassifier) extends Actor with ActorLogging{
    override def receive: Receive = {
      case (line:String, lineNo: Int) =>
        log.debug(s"$clientLogClassifier received #$lineNo")
        try {
          clientLogClassifier.classify(line, lineNo)
        } catch {
          case e: Exception => log.error(e,"Exception occurred in classification.")
        }
      case Manager.Shutdown =>
        clientLogClassifier.preStop()
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

  class FileSendActor(implicit val timeout: Timeout) extends Actor with RequiresMessageQueue[BoundedMessageQueueSemantics] with ActorLogging {
    final val INPUT_DIR = "inputDir"
    final val TARGET_DIR_REGEX = ClientLogSenderExecutor.CONFIG_BASE + ".target-dir-regex"
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
        val lineSenderActor = context.actorOf(FromConfig.props(LineSenderActor.props), "lineSender")
        // ファイル一覧の取得
        val files = FileUtils.autoClose(Files.walk(input)) {
          stream =>
            val iterator = stream.iterator().asScala
            iterator.filter(p => predicate(p)).toList.sortBy(parentName)
        }

        files.foreach(p => log.info(p.toString))
        for (file <- files) {
          log.info(s"ask call starts. file = $file")
          val fut = lineSenderActor ? file.toString
          try {
            val message = Await.result(fut, timeout.duration)
            log.info(s"ask call ends. message = $message")
          } catch {
            case e: TimeoutException =>
              log.error(e, "")
          }
          log.info(s"send All files are processed.")
          sender() ! "All files are processed."
        }
    }

    private def parentName(path: Path): String = path.getParent.getFileName.toString

    private def predicate(path: Path): Boolean = {
      if (Files.isDirectory(path))
        return false
      val regexOp = try {
        Some(config.getString(TARGET_DIR_REGEX))
      } catch {
        case _: Exception => None
      }
      regexOp.forall(parentName(path).matches(_))
    }
  }

  object FileSendActor {
    def props(implicit timeout: Timeout): Props = Props(new FileSendActor)
  }

  class LineSenderActor(implicit val timeout: Timeout) extends Actor with RequiresMessageQueue[BoundedMessageQueueSemantics] with ActorLogging {
    private val config = ConfigFactory.load()
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
        if (ZipUtils.isZipFile(file)) {
          log.info(s"unzip starts $file")
          val expandedDir = ZipUtils.unzip(file)
          log.info(s"unzip ends $file")
          val files2 = FileUtils.autoClose(Files.list(expandedDir)) { s => s.iterator().asScala.toList }
          for (file2 <- files2) {
            send(file2)
          }
          log.info(s"delete start $file")
          FileUtils.delete(expandedDir)
          log.info(s"delete end $file")
        } else {
          send(file)
        }
        sender() ! s"All line of $fileName processed."
    }

    private def send(file: Path): Unit = {
      OMSAplInfo.valueOf(file.getFileName.toString) match {
        case Some(aplInfo) =>
          log.info(s"$file is analyzing...")
          val clientLogClassifier = clientLogClassifierFactory.create(aplInfo)
          val actor = context.actorOf(ClientLogClassifierActor.props(clientLogClassifier),
            aplInfo.fileName)
          val stream = Files.lines(file, CHARSETNAME)
          for ((line, tmpNo) <- stream.iterator().asScala.zipWithIndex) {
            val lineNo = tmpNo + 1
            log.debug(s"send #$lineNo to $actor")
            actor ! (line, lineNo)
          }
          try {
            log.info("gracefulStop call starts.")
            val stopped = gracefulStop(actor, timeout.duration, Manager.Shutdown)
            Await.result(stopped, timeout.duration + 1.second)
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

  object LineSenderActor {
    def props(implicit timeout: Timeout): Props = {
      Props(new LineSenderActor)
    }
  }

}