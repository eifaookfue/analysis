package jp.co.nri.nefs.tool.analytics.sender.client

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector

object Keywords {
  val OBJ_EXTENSION = ".obj"
  val LOG_SUFFIX = "_Log"
  val WINDOW_DETAIL_SUFFIX = "_WindowDetail"
}

object ClientLogSenderExecutor extends ClientLogSenderComponent with ClientLogClassifierFactoryComponent
  with LazyLogging{
  implicit val system: ActorSystem = ActorSystem("ClientLogSender")
  val sender = new DefaultClientLogSender()
  ServiceInjector.initialize()
  val clientLogRecorder: ClientLogRecorder = ServiceInjector.getComponent(classOf[ClientLogRecorder])
  val clientLogClassifierFactory = new DefaultClientLogClassifyFactory(clientLogRecorder)

  def main(args: Array[String]): Unit = {

    val RECREATE_OPTION = "-recreate"
    val usage = s"Usage: jp.co.nri.nefs.tool.analytics.sender.client.ClientLogSenderExecutor [$RECREATE_OPTION]"

    if (args.length > 1){
      println(usage)
      sys.exit(1)
    }

    if (args.length == 1) {
      if (RECREATE_OPTION.equals(args(0))) {
        logger.info("recreate starting...")
        clientLogRecorder.recreate()
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
