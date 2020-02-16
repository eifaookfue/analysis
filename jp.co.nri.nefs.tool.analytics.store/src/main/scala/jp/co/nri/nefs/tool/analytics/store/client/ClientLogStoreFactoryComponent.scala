package jp.co.nri.nefs.tool.analytics.store.client

import java.nio.file.{Path, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.store.client.model.{Log, LogComponent, WindowDetail, WindowDetailComponent}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.basic.{BasicProfile, DatabaseConfig}
import slick.jdbc.JdbcProfile
import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait ClientLogStoreFactoryComponent {
  val clientLogStoreFactory: ClientLogStoreFactory

  trait ClientLogStoreFactory {
    def create(fileName: String): ClientLogStore
  }

  class DefaultClientLogStoreFactory extends ClientLogStoreFactory {
    private val config = ConfigFactory.load()
    private val outputDir: Path = Paths.get(config.getString(ConfigKey.OUT_DIR))
    def create(fileName: String): DefaultClientLogStore = {
      new DefaultClientLogStore(outputDir, fileName)
    }
  }

  trait ClientLogStore {
    def write(log: Log): Option[Long]
    def write(logId: Long, detail: WindowDetail): Unit
  }

  class DefaultClientLogStore(outputDir: Path = null, fileName: String = null,
    protected val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
      def get[P <: BasicProfile]: DatabaseConfig[P] = DatabaseConfig.forConfig[BasicProfile]("mydb").asInstanceOf[DatabaseConfig[P]]
    }) extends ClientLogStore with LogComponent with WindowDetailComponent with LazyLogging
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
