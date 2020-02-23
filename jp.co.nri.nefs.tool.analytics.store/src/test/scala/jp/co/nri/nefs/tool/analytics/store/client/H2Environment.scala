package jp.co.nri.nefs.tool.analytics.store.client

import com.google.inject.{AbstractModule, Guice, Injector}
import jp.co.nri.nefs.tool.analytics.model.OMSAplInfo
import jp.co.nri.nefs.tool.analytics.store.client.collector.ClientLogCollectorFactoryComponent
import jp.co.nri.nefs.tool.analytics.store.client.store.ClientLogStore
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.{BasicProfile, DatabaseConfig}

trait H2Environment extends ClientLogCollectorFactoryComponent {

  val injector: Injector = Guice.createInjector(new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[DatabaseConfigProvider]).to(classOf[H2DatabaseConfigProvider])
    }
  })
  val clientLogStore: ClientLogStore = injector.getInstance(classOf[ClientLogStore])
  val clientLogCollectorFactory = new DefaultClientLogCollectorFactory(clientLogStore)
  val omsAplInfo = OMSAplInfo("","","","","","20200216152000000")
  val clientLogCollector: DefaultClientLogCollector  = clientLogCollectorFactory.create(omsAplInfo).asInstanceOf[DefaultClientLogCollector]

}

class H2DatabaseConfigProvider extends DatabaseConfigProvider {
  override def get[P <: BasicProfile]: DatabaseConfig[P] =
    DatabaseConfig.forConfig[BasicProfile]("h2mem1").asInstanceOf[DatabaseConfig[P]]
}