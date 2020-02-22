package jp.co.nri.nefs.tool.analytics.store.client

import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Provider}
import jp.co.nri.nefs.tool.analytics.store.client.model.{Log, LogComponent, WindowDetail, WindowDetailComponent}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.basic.{BasicProfile, DatabaseConfig}
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait ClientLogStoreFactoryComponent {
  val clientLogStoreFactory: ClientLogStoreFactory
  @Inject
  val DefaultClientLogStoreProvider: Provider[DefaultClientLogStore]


  trait ClientLogStoreFactory {
    def create: ClientLogStore
  }

  class DefaultClientLogStoreFactory extends ClientLogStoreFactory {
    def create: DefaultClientLogStore = {

    }
  }

  trait ClientLogStore {
    def write(log: Log): Option[Long]
    def write(logId: Long, detail: WindowDetail): Unit
  }


  class DefaultDatabaseConfigProvider extends DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] =
      DatabaseConfig.forConfig[BasicProfile]("mydb")
      .asInstanceOf[DatabaseConfig[P]]
  }

  class DefaultClientLogStore @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    extends ClientLogStore with LogComponent with WindowDetailComponent with LazyLogging
    with HasDatabaseConfigProvider[JdbcProfile]{

    import profile.api._

    val logs = TableQuery[Logs]
    val windowDetails = TableQuery[WindowDetails]

    def write(log: Log): Option[Long] = {
      val action = (logs returning logs.map(_.logId)) += log
      try {
        val f = db.run(action)
        Some(Await.result(f, Duration.Inf))
      } catch {
        case e: Exception =>
          logger.info("", e)
          None
      }
    }

    def write(logId: Long, detail: WindowDetail): Unit = {
      val action = windowDetails += detail.copy(logId)
      val f = db.run(action)
      Await.result(f, Duration.Inf)
    }

  }
}
