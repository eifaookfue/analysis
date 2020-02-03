package jp.co.nri.nefs.tool.log.analysis

import java.nio.file.{Files, Path, Paths}

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.log.common.model.OMSAplInfo
import jp.co.nri.nefs.tool.log.common.utils.{FileUtils, ZipUtils}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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
      Future {
        val fileInfo = OMSAplInfo.valueOf(file.getFileName.toString) match {
          case Some(f) => f
          case None =>
            logger.debug(s"$file was not valid format, so skipped log sending.")
            return
        }
        val logAnalyzer = logAnalyzerFactory.create(fileInfo.fileName)
        val actor = system.actorOf(LogAnalyzerActor.props(logAnalyzer))
        FileUtils.autoClose(Files.lines(file)){stream =>
          for ((line, lineNo) <- stream.iterator().asScala.zipWithIndex) {
            actor ! (line, lineNo)
          }
        }
      }
    }

    def start(): Unit = {
      val stream = Files.list(input)
      for (file <- stream.iterator().asScala) {
        if (ZipUtils.isZipFile(file)) {
          val expandedDir = ZipUtils.unzip(file)
          val stream2 = Files.list(expandedDir)
          for (file2 <- stream2.iterator().asScala) {
            send(file2)
          }
          stream2.close()
          FileUtils.delete(expandedDir)
        } else {
          send(file)
        }
      }
    }
  }

  class LogAnalyzerActor(logAnalyzer: LogAnalyzer) extends Actor with ActorLogging{
    override def receive: Receive = {
      case (line:String, lineNo: Int) =>
        log.info(s"received $line")
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


