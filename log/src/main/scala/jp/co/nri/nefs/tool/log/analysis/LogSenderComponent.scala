package jp.co.nri.nefs.tool.log.analysis

import java.nio.file.{Files, Path, Paths}

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.log.common.model.OMSAplInfo
import jp.co.nri.nefs.tool.log.common.utils.{FileUtils, ZipUtils}

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

trait LogSenderComponent {
  self: LogAnalyzerFactoryComponent =>
  val sender: LogSender

  trait LogSender {
    def start(): Unit
  }

  class DefaultLogSender(implicit val system: ActorSystem) extends LogSender with LazyLogging{
    private val config = ConfigFactory.load()
    private val input = Paths.get(config.getString(ConfigKey.INPUT_DIR))

    private def send(file: Path): Unit = {
      OMSAplInfo.valueOf(file.getFileName.toString) match {
        case Some(fileInfo) =>
          logger.info(s"$file is analyzing...")
          val logAnalyzer = logAnalyzerFactory.create(fileInfo.fileName)
          val actor = system.actorOf(LogAnalyzerActor.props(logAnalyzer))
          val stream = Files.lines(file)
          for ((line, tmpNo) <- stream.iterator().asScala.zipWithIndex) {
            val lineNo = tmpNo + 1
            logger.info(s"send to $lineNo $line")
            actor ! (line, lineNo)
          }
          stream.close()
        case None =>
          logger.debug(s"$file was not valid format, so skipped log sending.")
      }
    }

    def start(): Unit = {
      val files = FileUtils.autoClose(Files.list(input)) { s => s.iterator().asScala.toList }
      val aggFut = Future.traverse(files) { file =>
        if (ZipUtils.isZipFile(file)) {
          val expandedDir = ZipUtils.unzip(file)
          val files2 = FileUtils.autoClose(Files.list(expandedDir)) { s => s.iterator().asScala.toList }
          val aggFut2 = Future.traverse(files2) { file2 =>
            Future(send(file2))
          }
          aggFut2.map(_ => FileUtils.delete(expandedDir))
        } else
          Future(send(file))
      }
      Await.ready(aggFut, Duration.Inf)
    }
  }

  class LogAnalyzerActor(logAnalyzer: LogAnalyzer) extends Actor with ActorLogging{
    override def receive: Receive = {
      case (line:String, lineNo: Int) =>
        log.debug(s"$logAnalyzer received {$lineNo} $line")
        logAnalyzer.analyze(line, lineNo)
      case _ =>
        log.info("received unknown message.")
    }
  }

  object LogAnalyzerActor {
    def props(logAnalyzer: LogAnalyzer): Props = {
      Props(new LogAnalyzerActor(logAnalyzer))
    }
  }

}


