package jp.co.nri.tcatool.common.upload

import com.typesafe.scalalogging.LazyLogging
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

trait DBUtil[T] extends LazyLogging {

  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  def query: TableQuery[_ <: Table[T]]

  def recreateTable(): Unit = {

    // https://stackoverflow.com/questions/40465252/slick-get-table-name
    val tableName = query.baseTableRow.tableName
    val schema = query.schema
    val drop = schema.dropIfExists
    val f1 = db.run(drop)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(_) => logger.info(s"$tableName table drop succeeded.")
      case Failure(e) => logger.error(s"$tableName table drop failed.", e)
    }
    val create = schema.createIfNotExists
    val f2 = db.run(create)
    Await.ready(f2, Duration.Inf)
    f2.value.get match {
      case Success(_) => logger.info(s"$tableName table create succeeded.")
      case Failure(e) => logger.error(s"$tableName table create failed", e)
    }
  }

  def upload(data: Seq[T]): Unit = {
    val tableName = query.baseTableRow.tableName
    val insert = query ++= data
    val f1 = db.run(insert)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(_) => logger.info(s"$tableName insert succeeded.")
      case Failure(e) => logger.error(s"$tableName insert failed.", e)
    }
  }

}
