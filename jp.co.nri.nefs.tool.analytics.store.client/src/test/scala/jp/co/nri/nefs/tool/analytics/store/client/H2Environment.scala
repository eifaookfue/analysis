package jp.co.nri.nefs.tool.analytics.store.client

import jp.co.nri.nefs.tool.analytics.model.client.OMSAplInfo
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector

trait H2Environment extends ClientLogClassifierFactoryComponent {

  ServiceInjector.initialize()
  val clientLogStore: ClientLogRecorder = ServiceInjector.getComponent(classOf[ClientLogRecorder])
  val clientLogClassifierFactory = new DefaultClientLogClassifierFactory(clientLogStore)
  val omsAplInfo = OMSAplInfo("","","","","","20200216152000000")
  val clientLogCollector: DefaultClientLogClassifier  = clientLogClassifierFactory.create(omsAplInfo).asInstanceOf[DefaultClientLogClassifier]

}