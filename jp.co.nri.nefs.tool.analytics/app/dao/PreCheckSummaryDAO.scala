package dao

import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.model.client.PreCheckSummaryComponent
import models.{PreCheckSummaryTbl, PreCheckTblRequestParams}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PreCheckSummaryDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends PreCheckSummaryComponent with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val preCheckSummaries = TableQuery[PreCheckSummaries]

  def count: Future[Int] = {
    db.run(preCheckSummaries.length.result)
  }

  def count(params: PreCheckTblRequestParams): Future[Int] = {
    db.run(filterQuery(params).length.result)
  }

  private def filterQuery(params: PreCheckTblRequestParams) = {
    for {
      p <- preCheckSummaries
      if Option(params.searchValue).filter(_.trim.nonEmpty).map { v =>
        (p.message like "%" + v + "%") || (p.windowName like "%" + v + "%")
      }.getOrElse(true: Rep[Boolean])
    } yield p
    /*preCheckSummaries.filter { p =>

    }*/
  }

  def list(params: PreCheckTblRequestParams): Future[Seq[PreCheckSummaryTbl]] = {
    val q1 = filterQuery(params)
    val q2 = q1.sortBy(v =>
    params.order0Column match {
      case 0 => if (params.order0Dir == "desc") v.message.desc else v.message.asc
      case 1 => if (params.order0Dir == "desc") v.windowName.desc else v.windowName.asc
      case 2 => if (params.order0Dir == "desc") v.count.desc else v.count.asc
      case _ => if (params.order0Dir == "desc") v.message.desc else v.message.asc
    })
    val q3 = q2.drop(params.start).take(params.length)
    val f = db.run(q3.result)
    f.map( seq => seq.map(p => PreCheckSummaryTbl(p.message, p.windowName, p.count)))
  }

}
