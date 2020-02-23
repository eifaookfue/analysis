package jp.co.nri.nefs.tool.analytics.store.client.store

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Guice, Injector}
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.store.client.collector.ClientLogCollectorFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.sender.ClientLogSenderComponent
import play.api.db.slick.DatabaseConfigProvider

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


object ClientLogStoreExecutor extends ClientLogSenderComponent with ClientLogCollectorFactoryComponent
  with LazyLogging{
  implicit val system: ActorSystem = ActorSystem("ClientLogCollector")
  val sender = new DefaultClientLogSender()
  val injector: Injector = Guice.createInjector(new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[DatabaseConfigProvider]).to(classOf[DefaultDatabaseConfigProvider])
    }
  })

  val clientLogStore: ClientLogStore = injector.getInstance(classOf[ClientLogStore])
  val clientLogCollectorFactory = new DefaultClientLogCollectorFactory(clientLogStore)

  def main(args: Array[String]): Unit = {

    val RECREATE_OPTION = "-recreate"
    val usage = s"Usage: jp.co.nri.nefs.tool.analytics.store.client [$RECREATE_OPTION]"

    if (args.length > 1){
      println(usage)
      sys.exit(1)
    }

    if (args.length == 1) {
      if (RECREATE_OPTION.equals(args(0))) {
        clientLogStore.recreate()
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
