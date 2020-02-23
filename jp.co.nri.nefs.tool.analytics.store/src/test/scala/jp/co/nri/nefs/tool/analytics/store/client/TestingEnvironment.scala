package jp.co.nri.nefs.tool.analytics.store.client

import com.google.inject.{AbstractModule, Guice, Injector}
import jp.co.nri.nefs.tool.analytics.model.OMSAplInfo
import jp.co.nri.nefs.tool.analytics.store.client.model.{Log, WindowDetail}
import scala.collection.mutable.ListBuffer

trait TestingEnvironment extends ClientLogCollectorFactoryComponent {

  val injector: Injector = Guice.createInjector(new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[ClientLogStore]).to(classOf[MockLogStore])
    }
  })
  val clientLogStore: ClientLogStore = injector.getInstance(classOf[ClientLogStore])

  val clientLogCollectorFactory = new DefaultClientLogCollectorFactory(clientLogStore)
  val omsAplInfo = OMSAplInfo("","","","","","20200216152000000")
  val clientLogCollector: DefaultClientLogCollector  = clientLogCollectorFactory.create(omsAplInfo).asInstanceOf[DefaultClientLogCollector]
  val output: ListBuffer[WindowDetail] = ListBuffer[WindowDetail]()

  class MockLogStore extends ClientLogStore {

    def recreate(): Unit = {}

    def write(log: Log): Option[Long] = {Some(0L)}

    def write(logId: Long, detail: WindowDetail): Unit = {
      output += detail
    }
  }
}