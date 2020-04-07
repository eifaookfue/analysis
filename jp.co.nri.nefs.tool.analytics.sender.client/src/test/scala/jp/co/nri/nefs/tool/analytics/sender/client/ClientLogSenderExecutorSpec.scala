package jp.co.nri.nefs.tool.analytics.sender.client

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import org.scalatest.FlatSpec

import scala.concurrent.duration.{Duration, FiniteDuration}

class ClientLogSenderExecutorSpec extends FlatSpec with ClientLogSenderComponent
  with LazyLogging with ClientLogClassifierFactoryComponent{
  implicit val system: ActorSystem = ActorSystem("ClientLogSender")
  final val WAIT_TIME_UNTIL_RECEIVER_ACK = "wait-time-until-receiver-ack"
  private val config = ConfigFactory.load()
  private val waitTimeUntilReceiverAck: FiniteDuration = Duration.fromNanos(config.getDuration(WAIT_TIME_UNTIL_RECEIVER_ACK).toNanos)
  implicit val timeout: Timeout = Timeout(waitTimeUntilReceiverAck)

  val sender = new DefaultClientLogSender()
  ServiceInjector.initialize()
  val clientLogRecorder: ClientLogRecorder = ServiceInjector.getComponent(classOf[ClientLogRecorder])
  val clientLogClassifierFactory = new DefaultClientLogClassifierFactory(clientLogRecorder)

  clientLogRecorder.recreate()

  try {
    sender.start()
  } catch {
    case e: Exception => logger.warn("", e)
  } finally {
    system.terminate()
  }

}