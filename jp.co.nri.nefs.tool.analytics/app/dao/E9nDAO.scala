package dao

import java.sql.Timestamp

import com.typesafe.config.Config
import common.Utilities
import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.model.client._
import jp.co.nri.nefs.tool.analytics.model.common.UserComponent
import models._
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class E9nDAO @Inject()(
                        protected val dbConfigProvider: DatabaseConfigProvider,
                        utilities: Utilities,
                        config: Configuration
                      )(implicit executionContext: ExecutionContext)
  extends E9nComponent with E9nStackTraceComponent with E9nDetailComponent with E9nCountComponent with E9nAuditComponent
    with E9nAuditHistoryComponent with LogComponent with UserComponent with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val e9ns = TableQuery[E9ns]
  val e9nStackTraces = TableQuery[E9nStackTraces]
  val e9nDetails = TableQuery[E9nDetails]
  val e9nCounts = TableQuery[E9nCounts]
  val e9nAudits = TableQuery[E9nAudits]
  val e9nAuditHistories = TableQuery[E9nAuditHistories]
  val logs = TableQuery[Logs]
  val users = TableQuery[Users]

  val conf: Config = config.underlying.getConfig(utilities.dbName)
  val convertFunction: String = conf.getString("dateToChar.function")
  val toChar: (Rep[Timestamp], Rep[String]) => Rep[String] =
    SimpleFunction.binary[Timestamp, String, String](convertFunction)

  def count: Future[Int] = {
    db.run(e9ns.length.result)
  }

  def count(params: E9nTblRequestParams): Future[Int] = {
    db.run(filterQuery(params).length.result)
  }

  private def filterQuery(params: E9nTblRequestParams) = {

    for {
      ((e, c), audit) <- e9ns joinLeft e9nCounts on (_.e9nId === _.e9nId) joinLeft e9nAudits on (_._1.e9nId === _.e9nId)
      if List(
        params.e9nIdSearchValue.map(e.e9nId === _),
        Option(params.headerSearchValue).filter(_.trim.nonEmpty).map(e.e9nHeadMessage like "%" + _ + "%"),
        params.countSearchValue.map(c.map(_.count).getOrElse(0) === _),
        params.statusSearchValue.map(s => (s: Rep[STATUS]) === audit.map(_.status).getOrElse(STATUS.NOT_YET: Rep[STATUS]))
      ).collect ({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
    } yield (e.e9nId, e.e9nHeadMessage, c.map(_.count),
      audit.map(_.status))
//      audit.map{a => LiftedE9nAudit(a.e9nId, a.status, a.comment, a.updatedBy, a.updateTime)}.map(_.status).getOrElse(STATUS.NOT_YET: Rep[STATUS]))
  }

  def e9nList(params: E9nTblRequestParams): Future[Seq[E9nTbl]] = {
    val q1 = filterQuery(params)
    val q2 = q1.sortBy { case (e9nId, e9nHeadMessage, count, status) =>
      params.order0Column match {
        case 0 => if (params.order0Dir == "desc") e9nId.desc else e9nId.asc
        case 1 => if (params.order0Dir == "desc") e9nHeadMessage.desc else e9nHeadMessage.asc
        case 3 => if (params.order0Dir == "desc") count.desc else count.asc
        case 4 => if (params.order0Dir == "desc") status.desc else status.asc
        case _ => if (params.order0Dir == "desc") e9nId.desc else e9nId.asc
      }
    }
//    val q2 = q1
    val q3 = q2.drop(params.start).take(params.length)
    val f = db.run(q3.result)
    f.map( seq => seq.map{ case (e9nId, message, count, status) =>
      E9nTbl(e9nId, message, count.getOrElse(0), status.getOrElse(STATUS.NOT_YET))
    })
  }

  def e9nStackTraceList(e9nId: Int): Future[Seq[E9nStackTrace]] = {
    val q = e9nStackTraces.filter(_.e9nId === e9nId).sortBy(_.number).map(_.e9nStackTraceProjection)
    db.run(q.result)
  }

  private def e9nDetailListQuery(params: E9nDetailTblRequestParams) = {
    val timeStr = params.timeSearchValue
    val formatter = utilities.formatter(timeStr)
    for {
      (((ed, l), e9), u) <- e9nDetails join logs on (_.logId === _.logId) join e9ns on (_._1.e9nId === _.e9nId) joinLeft users on (_._1._2.userId === _.userId)
      if List(
        Option(params.e9nIdSearchValue).filter(_.trim.nonEmpty).map(ed.e9nId === _.toInt),
        Option(params.logIdSearchValue).filter(_.trim.nonEmpty).map(ed.logId === _.toInt),
        Option(params.lineNoSearchValue).filter(_.trim.nonEmpty).map(ed.lineNo === _.toInt),
        Option(params.appNameSearchValue).filter(_.trim.nonEmpty).map(l.appName like "%" + _ + "%"),
        Option(params.userNameSearchValue).filter(_.trim.nonEmpty).map(u.map(_.userName).getOrElse(l.userId) like "%" + _ + "%"),
        Option(params.timeSearchValue).filter(_.trim.nonEmpty).map(l.tradeDate like "%" + _ + "%"),
        formatter.map(toChar(ed.time, _) === timeStr),
        Option(params.headMessageSearchValue).filter(_.trim.nonEmpty).map(e9.e9nHeadMessage like "%" + _ + "%"),
      ).collect({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
    } yield (ed.e9nId, ed.logId, ed.lineNo, l.appName, u.map(_.userName).getOrElse(l.userId), ed.time, e9.e9nHeadMessage)
  }

  def count(params: E9nDetailTblRequestParams): Future[Int] = {
    db.run(e9nDetailListQuery(params).length.result)
  }

  def e9nDetailList(params: E9nDetailTblRequestParams): Future[Seq[E9nDetailTbl]] = {
    val q1 = e9nDetailListQuery(params)
    val q2 = q1.sortBy { case (e9nId, logId, lineNo, appName, userName, time, e9nHeadMessage) =>
      params.order0Column match {
        case 0 => if (params.order0Dir == "desc") e9nId.desc else e9nId.asc
        case 1 => if (params.order0Dir == "desc") logId.desc else logId.asc
        case 2 => if (params.order0Dir == "desc") lineNo.desc else lineNo.asc
        case 3 => if (params.order0Dir == "desc") appName.desc else appName.asc
        case 4 => if (params.order0Dir == "desc") userName.desc else userName.asc
        case 5 => if (params.order0Dir == "desc") time.desc else time.asc
        case 6 => if (params.order0Dir == "desc") e9nHeadMessage.desc else e9nHeadMessage.asc
        case _ => if (params.order0Dir == "desc") e9nId.desc else e9nId.asc
      }
    }
    val q3 = q2.drop(params.start).take(params.length)
    val f = db.run(q3.result)
    f.map(seq => seq.map{case (e9nId, logId, lineNo, appName, userName, time, e9nHeadMessage) =>
      E9nDetailTbl(e9nId, logId, lineNo, appName, userName, time, e9nHeadMessage)
    })

  }

  def e9nAuditHistory(e9nId: Int): Future[Seq[E9nAuditHistoryEx]] = {
    val q = e9nAuditHistories.filter(_.e9nId === e9nId).sortBy(_.e9nHistoryId.desc)
    db.run(q.result)
  }

  def e9nAuditSave(auditInput: AuditInput): Future[Unit] = {
    val q = e9nAudits.filter(_.e9nId === auditInput.e9nId).map(_.auditProjection)
    val f = db.run(q.result)
    Await.ready(f, Duration.Inf)
    f.value.get match {
      case Success(v) =>
        val insertOrUpdate = v.headOption.map { a =>
          e9nAudits.filter(_.e9nId === auditInput.e9nId).map(_.auditProjection).update(
            a.copy(
              status = auditInput.status.getOrElse(a.status),
              comment = auditInput.comment.orElse(a.comment),
              updatedBy = auditInput.updatedBy
            )
          )
        }.getOrElse {
          e9nAudits.map(_.auditProjection) += E9nAudit(
            auditInput.e9nId,
            auditInput.status.getOrElse(STATUS.NOT_YET),
            auditInput.comment, auditInput.updatedBy
          )
        }
        val insertHistory = e9nAuditHistories.map(_.e9nAuditHistoryProjection) +=
          E9nAuditHistory(0, auditInput.e9nId, auditInput.status, auditInput.comment, auditInput.updatedBy)
        db.run(DBIO.seq(insertOrUpdate, insertHistory))
      case Failure(exception) =>
        Future.failed(exception)
    }
  }


}
