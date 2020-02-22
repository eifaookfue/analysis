package jp.co.nri.nefs.tool.analytics.store.client

import jp.co.nri.nefs.tool.analytics.model.OMSAplInfo
import jp.co.nri.nefs.tool.analytics.store.client.model.{Log, WindowDetail}

import scala.collection.mutable.ListBuffer

trait TestingEnvironment extends ClientLogCollectorFactoryComponent with ClientLogStoreComponent {

  val clientLogCollectorFactory = new DefaultClientLogCollectorFactory
  val clientLogStore = new MockLogStore
  val omsAplInfo = OMSAplInfo("","","","","","20200216152000000")
  val clientLogCollector: DefaultClientLogCollector  = clientLogCollectorFactory.create(omsAplInfo).asInstanceOf[DefaultClientLogCollector]
  val output: ListBuffer[WindowDetail] = ListBuffer[WindowDetail]()

  class MockLogStore extends ClientLogStore {

    def recreate: Unit = {}

    def write(log: Log): Option[Long] = {Some(0L)}

    def write(logId: Long, detail: WindowDetail): Unit = {
      output += detail
    }
  }
}