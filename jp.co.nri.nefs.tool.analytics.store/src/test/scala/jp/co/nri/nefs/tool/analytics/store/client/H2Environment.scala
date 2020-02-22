package jp.co.nri.nefs.tool.analytics.store.client

import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.model.OMSAplInfo
import jp.co.nri.nefs.tool.analytics.store.client.model.{Log, LogComponent, WindowDetail, WindowDetailComponent}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.basic.{BasicProfile, DatabaseConfig}
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait H2Environment extends ClientLogCollectorFactoryComponent with ClientLogStoreComponent {

  val clientLogCollectorFactory = new DefaultClientLogCollectorFactory
  val clientLogStore = new H2LogStore
  val omsAplInfo = OMSAplInfo("","","","","","20200216152000000")
  val clientLogCollector: DefaultClientLogCollector  = clientLogCollectorFactory.create(omsAplInfo).asInstanceOf[DefaultClientLogCollector]

  class H2LogStore(
                    protected val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
                      override def get[P <: BasicProfile]: DatabaseConfig[P] =
                        DatabaseConfig.forConfig[BasicProfile]("h2mem1").asInstanceOf[DatabaseConfig[P]]
                    }
                  ) extends ClientLogStore with LogComponent with WindowDetailComponent with LazyLogging
                  with HasDatabaseConfigProvider[JdbcProfile]{

    import profile.api._

    val logs = TableQuery[Logs]
    val windowDetails = TableQuery[WindowDetails]

    def recreate(): Unit = {
      for {tableQuery <- Seq(logs, windowDetails)}{
        val schema = tableQuery.schema
        println("create statements")
        schema.create.statements.foreach(println)
        val setup = DBIO.seq(
          //schema.dropIfExists,
          schema.createIfNotExists
        )
        val setupFuture = db.run(setup)
        Await.result(setupFuture, Duration.Inf)
      }
    }

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