package jp.co.nri.nefs.tool.analytics.store.client

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.store.client.sender.ClientLogSenderComponent

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


object ClientLogStore extends ClientLogSenderComponent with ClientLogCollectorFactoryComponent
  with ClientLogStoreFactoryComponent with LazyLogging{
  implicit val system: ActorSystem = ActorSystem("ClientLogCollector")
  val sender = new DefaultClientLogSender()
  val clientLogCollectorFactory = new DefaultClientLogCollectorFactory
  val clientLogStoreFactory = new DefaultClientLogStoreFactory

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
