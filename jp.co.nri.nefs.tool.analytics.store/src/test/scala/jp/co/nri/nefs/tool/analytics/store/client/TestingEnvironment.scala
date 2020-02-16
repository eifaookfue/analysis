package jp.co.nri.nefs.tool.analytics.store.client

import jp.co.nri.nefs.tool.analytics.model.OMSAplInfo
import jp.co.nri.nefs.tool.analytics.store.client.model.{Log, WindowDetail}

import scala.collection.mutable.ListBuffer

trait TestingEnvironment extends ClientLogCollectorFactoryComponent with ClientLogStoreFactoryComponent {

  val clientLogCollectorFactory = new DefaultClientLogCollectorFactory
  val clientLogStoreFactory = new MockWriterFactory
  val omsAplInfo = OMSAplInfo("","","","","","20200216152000000")
  val clientLogCollector: DefaultClientLogCollector  = clientLogCollectorFactory.create(omsAplInfo).asInstanceOf[DefaultClientLogCollector]
  val output: ListBuffer[WindowDetail] = ListBuffer[WindowDetail]()

  class MockWriterFactory extends ClientLogStoreFactory {
    def create(fileName: String): MockWriter = {
      new MockWriter
    }
  }

  class MockWriter extends ClientLogStore {
    def write(log: Log): Unit = {}

    def write(detail: WindowDetail): Unit = {
      output += detail
    }
  }
}