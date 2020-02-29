package jp.co.nri.nefs.tool.analytics.store.client.record

import com.google.inject.ImplementedBy
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.client.{Log, LogComponent, WindowDetail, WindowDetailComponent}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@ImplementedBy(classOf[DefaultClientLogRecorder])
trait ClientLogRecorder {
  def recreate(): Unit
  def write(log: Log): Option[Long]
  def write(logId: Long, detail: WindowDetail): Unit
}

class DefaultClientLogRecorder @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClientLogRecorder with LogComponent with WindowDetailComponent with LazyLogging
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
        schema.dropIfExists,
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
