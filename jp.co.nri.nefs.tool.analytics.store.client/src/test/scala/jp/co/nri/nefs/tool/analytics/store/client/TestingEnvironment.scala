package jp.co.nri.nefs.tool.analytics.store.client

import com.google.inject.AbstractModule
import jp.co.nri.nefs.tool.analytics.model.client._
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
  val preCheckOutput: ListBuffer[PreCheck] = clientLogRecorder.preCheckOutput
  val e9nStackTraceOutput: ListBuffer[E9nStackTrace] = clientLogRecorder.e9nStackTraceOutput

}

class MockLogRecorder extends ClientLogRecorder {

  val output: ListBuffer[WindowDetail] = ListBuffer[WindowDetail]()
  val preCheckOutput: ListBuffer[PreCheck] = ListBuffer()
  val e9nStackTraceOutput: ListBuffer[E9nStackTrace] = ListBuffer()
  val e9nAuditOutput: ListBuffer[E9nAudit] = ListBuffer()

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

  override def recordE9n(logId: Int, lineNo: Int, e9nStackTraceSeq: Seq[E9nStackTrace]): Future[Any] = {
    e9nStackTraceOutput ++= e9nStackTraceSeq
    Future.successful(0)
  }

  override def record(audit: E9nAudit): Future[Int] = {
    e9nAuditOutput += audit
    Future.successful(0)
  }

}