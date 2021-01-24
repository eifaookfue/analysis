package jp.co.nri.nefs.tool.analytics.generator.client

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.client._
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class TableCreator @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends LogComponent with WindowDetailComponent with PreCheckComponent
    with E9nComponent with E9nStackTraceComponent with E9nDetailComponent with E9nCountComponent
    with E9nAuditComponent with E9nAuditHistoryComponent
    with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging {

  import profile.api._
  import TableCreator._

  private val tables = Seq(
    TableQuery[Logs],
    TableQuery[WindowDetails],
    TableQuery[PreChecks],
    TableQuery[E9ns],
    TableQuery[E9nStackTraces],
    TableQuery[E9nDetails],
    TableQuery[E9nCounts],
    TableQuery[E9nAudits],
    TableQuery[E9nAuditHistories]
  ).filter(q => tableNames.contains(q.baseTableRow.tableName))

  def recreate(): Unit = {
    for {tableQuery <- tables}{
      val schema = tableQuery.schema
      val tableName = tableQuery.baseTableRow.tableName
      val drop = schema.dropIfExists
      val f1 = db.run(drop)
      Await.ready(f1, Duration.Inf)
      f1.value.get match {
        case Success(_) => logger.info(s"Table $tableName drop succeeded.")
        case Failure(e) => logger.error(s"Table $tableName drop failed", e)
      }

      val create = schema.createIfNotExists
      val f2 = db.run(create)
      Await.ready(f2, Duration.Inf)
      f2.value.get match {
        case Success(_) => logger.info(s"Table $tableName creation succeeded.")
        case Failure(e) => logger.error(s"Table $tableName creation failed", e)
      }
    }
  }


}

object TableCreator extends LazyLogging {

  final val TABLE_CREATOR = "TableCreator"
  final val CREATION_TABLES = "creation-tables"
  private val config = ConfigFactory.load().getConfig(TABLE_CREATOR)

  import jp.co.nri.nefs.tool.util.config.RichConfig._

  logger.info(config.toString)
  private val tableNames = config.getStringList(CREATION_TABLES, logger)

  def main(args: Array[String]): Unit = {
    ServiceInjector.initialize()
    val tableCreator = ServiceInjector.getComponent(classOf[TableCreator])
    tableCreator.recreate()
  }
}
