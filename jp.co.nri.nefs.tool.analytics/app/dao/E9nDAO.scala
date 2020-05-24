package dao

import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.model.client._
import models.E9nTblRequestParams
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class E9nDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends E9nComponent with E9nStackTraceComponent with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val e9ns = TableQuery[E9ns]
  val e9nStackTraces = TableQuery[E9nStackTraces]

  def count: Future[Int] = {
    db.run(e9ns.length.result)
  }

  def count(params: E9nTblRequestParams): Future[Int] = {
    db.run(filterQuery(params).length.result)
  }

  private def filterQuery(params: E9nTblRequestParams) = {
    for {
      e <- e9ns
      if Option(params.searchValue).filter(_.trim.nonEmpty).map(e.e9nHeadMessage like "%" + _  + "%").getOrElse(true: Rep[Boolean])
    } yield e
  }

  def e9nList(params: E9nTblRequestParams): Future[Seq[E9n]] = {
    val q1 = filterQuery(params)
    val q2 = q1.sortBy(e =>
      params.order0Column match {
        case 0 => if (params.order0Dir == "desc") e.e9nId.desc else e.e9nId.asc
        case 1 => if (params.order0Dir == "desc") e.e9nHeadMessage.desc else e.e9nHeadMessage.asc
        case 2 => if (params.order0Dir == "desc") e.count.desc else e.count.asc
        case _ => if (params.order0Dir == "desc") e.e9nId.desc else e.e9nId.asc
      }
    )
    val q3 = q2.drop(params.start).take(params.length)
    db.run(q3.result)
  }

  def e9nStackTraceList(e9nId: Int): Future[Seq[E9nStackTrace]] = {
    val q = e9nStackTraces.filter(_.e9nId === e9nId).sortBy(_.number)
    db.run(q.result)
  }
}
