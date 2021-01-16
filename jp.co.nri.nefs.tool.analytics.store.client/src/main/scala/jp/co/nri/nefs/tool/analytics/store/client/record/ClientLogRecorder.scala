package jp.co.nri.nefs.tool.analytics.store.client.record

import java.sql.Timestamp
import com.google.inject.{ImplementedBy, Inject}
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.model.client._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

@ImplementedBy(classOf[DefaultClientLogRecorder])
trait ClientLogRecorder {
  def recreate(): Unit
  def record(log: Log): Option[Int]
  def record(logId: Int, detail: WindowDetail): Future[Int]
  def record(preCheck: PreCheck): Future[Int]
  def recordE9n(logId: Int, lineNo: Int, time: Timestamp, e9nStackTraceSeq: Seq[E9nStackTrace]): Future[Any]
  def record(audit: E9nAudit): Future[Int]
}

class DefaultClientLogRecorder @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClientLogRecorder with LogComponent with WindowDetailComponent with PreCheckComponent
  with E9nComponent with E9nStackTraceComponent with E9nDetailComponent with E9nCountComponent
  with E9nAuditComponent
    with LazyLogging
    with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val preChecks = TableQuery[PreChecks]
  val e9ns = TableQuery[E9ns]
  val e9nStackTraces = TableQuery[E9nStackTraces]
  val e9nDetails = TableQuery[E9nDetails]
  val e9nCounts = TableQuery[E9nCounts]
  val e9nAudits = TableQuery[E9nAudits]

  def recreate(): Unit = {
    for {tableQuery <- Seq(logs, windowDetails, preChecks, e9ns, e9nStackTraces, e9nDetails, e9nCounts, e9nAudits)}{
      val schema = tableQuery.schema
      logger.debug("create statements")
      schema.create.statements.foreach(s => logger.debug(s))

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

  def record(log: Log): Option[Int] = {
    val action = (logs.map(_.logProjection) returning logs.map(_.logId)) += log
    val f = db.run(action)
    Await.ready(f, Duration.Inf)
    f.value.get match {
      case Success(value) => Some(value)
      case Failure(e) =>
        logger.error("failed.", e)
        None
    }
  }

  def record(logId: Int, detail: WindowDetail): Future[Int] = {
    logger.info(s"storing $detail")
    val insert = windowDetails.map(_.windowDetailProjection) += detail.copy(logId = logId)
    db.run(insert)
  }

  def record(preCheck: PreCheck): Future[Int] = {
    val insert = preChecks.map(_.preCheckProjection) += preCheck
    db.run(insert)
  }

  override def recordE9n(logId: Int, lineNo: Int, time: Timestamp, e9nStackTraceSeq: Seq[E9nStackTrace]): Future[Any] = {

    val headMessage = e9nStackTraceSeq.head.message
    val length = e9nStackTraceSeq.map(_.message).mkString.length
    val q1 = for {
      (e, c) <- e9ns joinLeft e9nCounts on (_.e9nId === _.e9nId)
      if e.e9nHeadMessage === headMessage && e.e9nLength === length
    } yield (e.e9nId, c.map(_.count))

    val f1 = db.run(q1.result.headOption)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(e9nIdAndCount) =>
        e9nIdAndCount match {
          // If found from E9N table, use e9nId from the table
          case Some((e9nId, countOp)) =>
            val insertE9nDetail = e9nDetails.map(_.e9nDetailProjection) += E9nDetail(logId, lineNo, e9nId, time)
            val insertOrUpdateE9nCount = countOp match {
              // If found from E9N_COUNT table, count up
              case Some(count) =>
                e9nCounts.filter(_.e9nId === e9nId).map(_.count).update(count + 1)
              // If no records found from E9N_COUNT table, insert with count = 1
              case None =>
                e9nCounts.map(e => (e.e9nId, e.count)) += (e9nId, 1)
            }
            db.run(DBIO.seq(insertE9nDetail, insertOrUpdateE9nCount))
          // If not found from E9N table, insert to E9N_TABLE first
          case None =>
            val insertE9n = e9ns.map(e => (e.e9nId, e.e9nHeadMessage, e.e9nLength)) returning e9ns.map(_.e9nId) +=
              (0, headMessage, length)
            val f2 = db.run(insertE9n)
            Await.ready(f2, Duration.Inf)
            f2.value.get match {
              case Success(e9nId) =>
                val insertE9nDetail = e9nDetails.map(_.e9nDetailProjection) += E9nDetail(logId, lineNo, e9nId, time)
                val insertE9nStackTrace = e9nStackTraces.map(_.e9nStackTraceProjection) ++= e9nStackTraceSeq.map(_.copy(e9nId = e9nId))
                val insertE9nCount = e9nCounts.map(e => (e.e9nId, e.count)) += (e9nId, 1)
                db.run(DBIO.seq(insertE9nDetail, insertE9nStackTrace, insertE9nCount))
              case Failure(e) =>
                Future.failed(e)
            }
        }
      case Failure(e) =>
        Future.failed(e)
    }
  }

  override def record(audit: E9nAudit): Future[Int] = {
    val insert = e9nAudits.map(_.auditProjection) += audit
    db.run(insert)
  }

}
