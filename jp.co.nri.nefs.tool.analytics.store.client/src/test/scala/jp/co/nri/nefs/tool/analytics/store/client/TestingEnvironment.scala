package jp.co.nri.nefs.tool.analytics.store.client

import com.google.inject.AbstractModule
import jp.co.nri.nefs.tool.analytics.model.client.{Log, OMSAplInfo, PreCheck, WindowDetail}
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

trait TestingEnvironment extends ClientLogClassifierFactoryComponent {

  val _ = ServiceInjector.initialize(new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[ClientLogRecorder]).to(classOf[MockLogRecorder])
    }
  })

  //val clientLogRecorder: ClientLogRecorder = ServiceInjector.getComponent(classOf[ClientLogRecorder])

  val clientLogRecorder = new MockLogRecorder
  val clientLogClassifierFactory = new DefaultClientLogClassifierFactory(clientLogRecorder)
  val omsAplInfo = OMSAplInfo("","","","","","20200216152000000")
  val clientLogClassifier: DefaultClientLogClassifier  = clientLogClassifierFactory.create(omsAplInfo).asInstanceOf[DefaultClientLogClassifier]
  val output: ListBuffer[WindowDetail] = clientLogRecorder.output

}

class MockLogRecorder extends ClientLogRecorder {

  val output: ListBuffer[WindowDetail] = ListBuffer[WindowDetail]()
  val preCheckOutput: ListBuffer[PreCheck] = ListBuffer()

  def recreate(): Unit = {}

  def record(log: Log): Option[Int] = {Some(0)}

  def record(logId: Int, detail: WindowDetail): Future[Int] = {
    output += detail
    Future.successful(0)
  }

  def record(preCheck: PreCheck): Future[Int] = {
    preCheckOutput += preCheck
    Future.successful(0)
  }
}