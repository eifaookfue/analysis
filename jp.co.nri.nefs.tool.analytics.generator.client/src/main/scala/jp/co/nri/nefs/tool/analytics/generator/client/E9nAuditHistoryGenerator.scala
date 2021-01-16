package jp.co.nri.nefs.tool.analytics.generator.client

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.client.E9nAuditHistoryComponent
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class E9nAuditHistoryGenerator @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends E9nAuditHistoryComponent with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging {

  import profile.api._

  val e9nAuditHistories = TableQuery[E9nAuditHistories]

  def create(): Unit = {
    val create = e9nAuditHistories.schema.createIfNotExists
    val f = db.run(create)
    Await.ready(f, Duration.Inf)
    f.value.get match {
      case Success(_) =>
        logger.info(s"${e9nAuditHistories.baseTableRow.tableName} creation succeeded.")
      case Failure(e) =>
        logger.error(s"${e9nAuditHistories.baseTableRow.tableName} creation failed.", e)
    }
  }

}

object E9nAuditHistoryGenerator {
  def main(args: Array[String]): Unit = {

    ServiceInjector.initialize()
    val generator = ServiceInjector.getComponent(classOf[E9nAuditHistoryGenerator])
    generator.create()
  }
}
