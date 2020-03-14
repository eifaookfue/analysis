package jp.co.nri.nefs.tool.analytics.store.client.record

import com.google.inject.ImplementedBy
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.client._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

@ImplementedBy(classOf[DefaultClientLogRecorder])
trait ClientLogRecorder {
  def recreate(): Unit
  def record(log: Log): Option[Int]
  def record(logId: Int, detail: WindowDetail): Future[Int]
  def record(preCheck: PreCheck): Future[Int]
}

class DefaultClientLogRecorder @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClientLogRecorder with LogComponent with WindowDetailComponent with PreCheckComponent
    with LazyLogging
    with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val preChecks = TableQuery[PreChecks]

  def recreate(): Unit = {
    for {tableQuery <- Seq(logs, windowDetails, preChecks)}{
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

  def record(log: Log): Option[Int] = {
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

  def record(logId: Int, detail: WindowDetail): Future[Int] = {
    val insert = windowDetails += detail.copy(logId)
    db.run(insert)
  }

  def record(preCheck: PreCheck): Future[Int] = {
    val insert = preChecks += preCheck
    db.run(insert)
  }

}
