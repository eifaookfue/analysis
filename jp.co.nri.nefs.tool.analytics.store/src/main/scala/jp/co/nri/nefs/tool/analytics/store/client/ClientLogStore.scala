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
  with ClientLogStoreComponent with LazyLogging{
  implicit val system: ActorSystem = ActorSystem("ClientLogCollector")
  val sender = new DefaultClientLogSender()
  val clientLogCollectorFactory = new DefaultClientLogCollectorFactory
  val clientLogStore = new DefaultClientLogStore

  def main(args: Array[String]): Unit = {

    val RECREATE_OPTION = "-recreate"
    val usage = s"Usage: jp.co.nri.nefs.tool.analytics.store.client [$RECREATE_OPTION]"

    if (args.length > 1){
      println(usage)
      sys.exit(1)
    }

    if (args.length == 1) {
      if (RECREATE_OPTION.equals(args(1))) {
        clientLogStore.recreate
      } else {
        println(usage)
        sys.exit(1)
      }
    }

    try {
      sender.start()
    } catch {
      case e: Exception => logger.warn("", e)
    } finally {
      system.terminate()
    }

  }
}
