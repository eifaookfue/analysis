package jp.co.nri.nefs.tool.analytics.store.client

import com.google.inject.AbstractModule
import jp.co.nri.nefs.tool.analytics.model.client.OMSAplInfo
import jp.co.nri.nefs.tool.analytics.store.client.classify.ClientLogClassifierFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.{BasicProfile, DatabaseConfig}

trait H2Environment extends ClientLogClassifierFactoryComponent {

  ServiceInjector.initialize()
  val clientLogStore: ClientLogRecorder = ServiceInjector.getComponent(classOf[ClientLogRecorder])
  val clientLogClassifierFactory = new DefaultClientLogClassifyFactory(clientLogStore)
  val omsAplInfo = OMSAplInfo("","","","","","20200216152000000")
  val clientLogCollector: DefaultClientLogClassifier  = clientLogClassifierFactory.create(omsAplInfo).asInstanceOf[DefaultClientLogClassifier]

}

class H2DatabaseConfigProvider extends DatabaseConfigProvider {
  override def get[P <: BasicProfile]: DatabaseConfig[P] =
    DatabaseConfig.forConfig[BasicProfile]("h2mem1").asInstanceOf[DatabaseConfig[P]]
}