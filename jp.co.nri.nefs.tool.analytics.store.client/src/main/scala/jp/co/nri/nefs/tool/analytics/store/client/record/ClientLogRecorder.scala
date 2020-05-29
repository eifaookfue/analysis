package jp.co.nri.nefs.tool.analytics.store.client.record

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
  def recordE9n(logId: Int, lineNo: Int, e9nStackTraceSeq: Seq[E9nStackTrace]): Future[Any]
}

class DefaultClientLogRecorder @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClientLogRecorder with LogComponent with WindowDetailComponent with PreCheckComponent
  with E9nComponent with E9nStackTraceComponent with E9nDetailComponent
    with LazyLogging
    with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val preChecks = TableQuery[PreChecks]
  val e9ns = TableQuery[E9ns]
  val e9nStackTraces = TableQuery[E9nStackTraces]
  val e9nDetails = TableQuery[E9nDetails]

  def recreate(): Unit = {
    for {tableQuery <- Seq(logs, windowDetails, preChecks, e9ns, e9nStackTraces, e9nDetails)}{
      val schema = tableQuery.schema
      logger.debug("create statements")
      schema.create.statements.foreach(s => logger.debug(s))

      val drop = schema.dropIfExists
      val f1 = db.run(drop)
      Await.ready(f1, Duration.Inf)
      f1.value.get match {
        case Success(_) => logger.info("drop succeeded.")
        case Failure(e) => logger.error("drop failed", e)
      }

      val create = schema.createIfNotExists
      val f2 = db.run(create)
      Await.ready(f2, Duration.Inf)
      f2.value.get match {
        case Success(_) => logger.info("create succeeded.")
        case Failure(e) => logger.error("create failed", e)
      }
    }
  }

  def record(log: Log): Option[Int] = {
    val action = (logs.map(l => (l.logId, l.appName, l.computerName, l.userId, l.tradeDate, l.time, l.fileName))
      returning logs.map(_.logId)) += (log.logId, log.appName, log.computerName, log.userId, log.tradeDate, log.time, log.fileName)
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
    logger.info(s"storing $detail")
    val insert = windowDetails.map(w => (w.logId, w.lineNo, w.activator, w.windowName, w.destinationType, w.action, w.method, w.time, w.startupTime)) +=
      (logId, detail.lineNo, detail.activator, detail.windowName, detail.destinationType, detail.action, detail.method, detail.time, detail.startupTime)
    db.run(insert)
  }

  def record(preCheck: PreCheck): Future[Int] = {
    val insert = preChecks.map(p => (p.logId, p.lineNo, p.windowName, p.code, p.message)) +=
      (preCheck.logId, preCheck.lineNo, preCheck.windowName, preCheck.code, preCheck.message)
    db.run(insert)
  }

  override def recordE9n(logId: Int, lineNo: Int, e9nStackTraceSeq: Seq[E9nStackTrace]): Future[Any] = {

    val headMessage = e9nStackTraceSeq.head.message
    val length = e9nStackTraceSeq.map(_.message).mkString.length
    val q1 = e9ns.filter(e => e.e9nHeadMessage === headMessage && e.e9nLength === length).map(e => (e.e9nId, e.count))
    val f1 = db.run(q1.result.headOption)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(op) =>
        op match {
          case Some((e9nId, count)) =>
            val updateE9n = e9ns.filter(_.e9nId === e9nId).map(_.count).update(count + 1)
            val insertE9nDetail = e9nDetails += E9nDetail(logId, lineNo, e9nId)
            db.run(DBIO.seq(updateE9n, insertE9nDetail))
          case None =>
            val insertE9n = e9ns.map(e => (e.e9nId, e.e9nHeadMessage, e.e9nLength, e.count)) returning e9ns.map(_.e9nId) += (0, headMessage, length, 1)
            val f2 = db.run(insertE9n)
            Await.ready(f2, Duration.Inf)
            f2.value.get match {
              case Success(e9nId) =>
                val insertE9nStackTrace = e9nStackTraces.map(e => (e.e9nId, e.number, e.message)) ++= e9nStackTraceSeq.map(e => (e9nId, e.number, e.message))
                val insertE9nDetail = e9nDetails.map(e => (e.logId, e.lineNo, e.e9nId)) += (logId, lineNo, e9nId)
                db.run(DBIO.seq(insertE9nStackTrace, insertE9nDetail))
              case Failure(e) =>
                Future.failed(e)
            }

        }
      case Failure(e) =>
        Future.failed(e)
    }

  }

}
