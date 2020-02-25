package jp.co.nri.nefs.tool.analytics.store.client

import com.google.inject.AbstractModule
import jp.co.nri.nefs.tool.analytics.model.client.{Log, OMSAplInfo, WindowDetail}
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector

import scala.collection.mutable.ListBuffer

trait TestingEnvironment extends ClientLogClassifierFactoryComponent {

  val _ = ServiceInjector.initialize(new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[ClientLogRecorder]).to(classOf[MockLogRecorder])
    }
  })

  //val clientLogRecorder: ClientLogRecorder = ServiceInjector.getComponent(classOf[ClientLogRecorder])

  val clientLogRecorder = new MockLogRecorder
  val clientLogClassifierFactory = new DefaultClientLogClassifyFactory(clientLogRecorder)
  val omsAplInfo = OMSAplInfo("","","","","","20200216152000000")
  val clientLogCollector: DefaultClientLogCollector  = clientLogClassifierFactory.create(omsAplInfo).asInstanceOf[DefaultClientLogCollector]
  val output: ListBuffer[WindowDetail] = clientLogRecorder.output

}

class MockLogRecorder extends ClientLogRecorder {

  val output: ListBuffer[WindowDetail] = ListBuffer[WindowDetail]()

  def recreate(): Unit = {}

  def write(log: Log): Option[Long] = {Some(0L)}

  def write(logId: Long, detail: WindowDetail): Unit = {
    output += detail
  }
}