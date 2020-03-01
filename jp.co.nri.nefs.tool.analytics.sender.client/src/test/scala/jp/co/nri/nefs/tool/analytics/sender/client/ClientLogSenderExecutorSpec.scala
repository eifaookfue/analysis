package jp.co.nri.nefs.tool.analytics.sender.client

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import org.scalatest.FlatSpec

class ClientLogSenderExecutorSpec extends FlatSpec with ClientLogSenderComponent
  with LazyLogging with ClientLogClassifierFactoryComponent{
  implicit val system: ActorSystem = ActorSystem("ClientLogSender")
  val sender = new DefaultClientLogSender()
  ServiceInjector.initialize()
  val clientLogRecorder: ClientLogRecorder = ServiceInjector.getComponent(classOf[ClientLogRecorder])
  val clientLogClassifierFactory = new DefaultClientLogClassifyFactory(clientLogRecorder)

  clientLogRecorder.recreate()

  try {
    sender.start()
  } catch {
    case e: Exception => logger.warn("", e)
  } finally {
    system.terminate()
  }

}