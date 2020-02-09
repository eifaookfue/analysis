package jp.co.nri.nefs.tool.log.analysis

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

object Keywords {
  val OBJ_EXTENSION = ".obj"
  val LOG_SUFFIX = "_Log"
  val WINDOW_DETAIL_SUFFIX = "_WindowDetail"
}

object ConfigKey {
  final val HANDLER_MAPPING = "HandlerMapping"
  final val WINDOW_MAPPING = "WindowMapping"
  final val INPUT_DIR = "inputDir"
  final val OUT_DIR = "outDir"
}


object LogAnalyzer extends LogSenderComponent with LogAnalyzerFactoryComponent
  with AnalysisWriterComponent with LazyLogging{
  implicit val system: ActorSystem = ActorSystem("LogAnalyzer")
  val sender = new DefaultLogSender()
  val logAnalyzerFactory = new DefaultLogAnalyzerFactory
  val analysisWriterFactory = new DefaultAnalysisWriterFactory

  def main(args: Array[String]): Unit = {
    try {
      sender.start()
    } catch {
      case e: Exception => logger.warn("", e)
    } finally {
      system.terminate()
    }

  }
}
