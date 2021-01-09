package dao

import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.model.client._
import jp.co.nri.nefs.tool.analytics.model.common.UserComponent
import models.{E9nDetailTbl, E9nDetailTblRequestParams, E9nTbl, E9nTblRequestParams}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class E9nDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends E9nComponent with E9nStackTraceComponent with E9nDetailComponent with E9nCountComponent with E9nAuditComponent
    with LogComponent with UserComponent with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val e9ns = TableQuery[E9ns]
  val e9nStackTraces = TableQuery[E9nStackTraces]
  val e9nDetails = TableQuery[E9nDetails]
  val e9nCounts = TableQuery[E9nCounts]
  val e9nAudits = TableQuery[E9nAudits]
  val logs = TableQuery[Logs]
  val users = TableQuery[Users]

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
        Option(params.statusSearchValue).map(s => audit.map(_.status).getOrElse(null) === s)
      ).collect ({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
    } yield (e.e9nId, e.e9nHeadMessage, c.map(_.count),
      audit.map(_.status))
//      audit.map{a => LiftedE9nAudit(a.e9nId, a.status, a.comment, a.updatedBy, a.updateTime)}.map(_.status).getOrElse(STATUS.NOT_YET: Rep[STATUS]))
  }

  def e9nList(params: E9nTblRequestParams): Future[Seq[E9nTbl]] = {
    println(s"params=$params")
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
      E9nTbl(e9nId, message, count.getOrElse(0), status.getOrElse(STATUS.DONE))
    })
  }

  def e9nStackTraceList(e9nId: Int): Future[Seq[E9nStackTrace]] = {
    val q = e9nStackTraces.filter(_.e9nId === e9nId).sortBy(_.number).map(_.e9nStackTraceProjection)
    db.run(q.result)
  }

  private def e9nDetailListQuery(params: E9nDetailTblRequestParams) = {
    for {
      (((ed, l), e9), u) <- e9nDetails join logs on (_.logId === _.logId) join e9ns on (_._1.e9nId === _.e9nId) joinLeft users on (_._1._2.userId === _.userId)
      if List(
        Option(params.col0SearchValue).filter(_.trim.nonEmpty).map(ed.e9nId === _.toInt),
        Option(params.col1SearchValue).filter(_.trim.nonEmpty).map(ed.logId === _.toInt),
        Option(params.col2SearchValue).filter(_.trim.nonEmpty).map(ed.lineNo === _.toInt),
        Option(params.col3SearchValue).filter(_.trim.nonEmpty).map(l.appName like "%" + _ + "%"),
        Option(params.col4SearchValue).filter(_.trim.nonEmpty).map(u.map(_.userName).getOrElse(l.userId) like "%" + _ + "%"),
        Option(params.col5SearchValue).filter(_.trim.nonEmpty).map(e9.e9nHeadMessage like "%" + _ + "%"),
      ).collect({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
    } yield (ed.e9nId, ed.logId, ed.lineNo, l.appName, u.map(_.userName).getOrElse(l.userId), e9.e9nHeadMessage)
  }

  def count(params: E9nDetailTblRequestParams): Future[Int] = {
    db.run(e9nDetailListQuery(params).length.result)
  }

  def e9nDetailList(params: E9nDetailTblRequestParams): Future[Seq[E9nDetailTbl]] = {
    val q1 = e9nDetailListQuery(params)
    val q2 = q1.sortBy { case (e9nId, logId, lineNo, appName, userName, e9nHeadMessage) =>
      params.order0Column match {
        case 0 => if (params.order0Dir == "desc") e9nId.desc else e9nId.asc
        case 1 => if (params.order0Dir == "desc") logId.desc else logId.asc
        case 2 => if (params.order0Dir == "desc") lineNo.desc else lineNo.asc
        case 3 => if (params.order0Dir == "desc") appName.desc else appName.asc
        case 4 => if (params.order0Dir == "desc") userName.desc else userName.asc
        case 5 => if (params.order0Dir == "desc") e9nHeadMessage.desc else e9nHeadMessage.asc
        case _ => if (params.order0Dir == "desc") e9nId.desc else e9nId.asc
      }
    }
    val q3 = q2.drop(params.start).take(params.length)
    val f = db.run(q3.result)
    f.map(seq => seq.map{case (e9nId, logId, lineNo, appName, userName, e9nHeadMessage) =>
      E9nDetailTbl(e9nId, logId, lineNo, appName, userName, e9nHeadMessage)
    })

  }

  def e9nAudit(e9nId: Int): Future[Seq[E9nAuditEx]] = {
    val q = e9nAudits.filter(_.e9nId === e9nId)
    db.run(q.result)
  }


}
