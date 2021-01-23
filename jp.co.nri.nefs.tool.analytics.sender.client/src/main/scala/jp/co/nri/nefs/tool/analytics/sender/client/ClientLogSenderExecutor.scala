package jp.co.nri.nefs.tool.analytics.sender.client

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.io.StdIn

object Keywords {
  val OBJ_EXTENSION = ".obj"
  val LOG_SUFFIX = "_Log"
  val WINDOW_DETAIL_SUFFIX = "_WindowDetail"
}

object ClientLogSenderExecutor extends ClientLogSenderComponent with ClientLogClassifierFactoryComponent
  with LazyLogging{

  implicit val system: ActorSystem = ActorSystem("ClientLogSender")
  final val CONFIG_BASE = "client-log-sender"
  final val IS_RECREATE = CONFIG_BASE + ".is-recreate"
  final val WAIT_TIME_UNTIL_ALL_FILES_EXECUTION = CONFIG_BASE + ".wait-time-until-all-files-execution"
  private val config = ConfigFactory.load()
  private val isRecreate = config.getBoolean(IS_RECREATE)
  private val waitTimeUntilAllFilesExecution: FiniteDuration = Duration.fromNanos(config.getDuration(WAIT_TIME_UNTIL_ALL_FILES_EXECUTION).toNanos)
  implicit val timeout: Timeout = Timeout(waitTimeUntilAllFilesExecution)
  logger.info(s"sender before start")
  val sender = new DefaultClientLogSender()
  logger.info(s"sender after start")
  ServiceInjector.initialize()
  logger.info("initialize end")
  val clientLogRecorder: ClientLogRecorder = ServiceInjector.getComponent(classOf[ClientLogRecorder])
  val clientLogClassifierFactory = new DefaultClientLogClassifierFactory(clientLogRecorder)

  def main(args: Array[String]): Unit = {

    logger.info(s"isRecreate=$isRecreate")

    if (isRecreate) {
      logger.info("recreate starting...")
      clientLogRecorder.recreate()
    }

    try {
      sender.start()
      logger.info("wait another 3 seconds starts.")
      Thread.sleep(3000)
      logger.info("wait another 3 seconds done.")
      //StdIn.readLine()
    } catch {
      case e: Exception => logger.warn("", e)
    } finally {
      system.terminate()
    }

  }
}