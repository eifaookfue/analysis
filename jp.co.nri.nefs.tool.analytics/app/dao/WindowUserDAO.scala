package dao

import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.model.client.WindowUserComponent
import jp.co.nri.nefs.tool.analytics.model.common.UserComponent
import models.{WindowCountByUser, WindowSliceTblRequestParams}
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class WindowUserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, config: Configuration)(implicit executionContext: ExecutionContext)
  extends WindowUserComponent with UserComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val windowUsers = TableQuery[WindowUsers]
  val users = TableQuery[Users]

  def count: Future[Int] = {
    db.run(windowUsers.length.result)
  }

  def count(params: WindowSliceTblRequestParams): Future[Int] = {
    db.run(filterQuery(params).length.result)
  }

  private def filterQuery(params: WindowSliceTblRequestParams) = {
    for {
      (w, u) <- windowUsers joinLeft users on (_.userId === _.userId)
      if Option(params.searchValue).filter(_.trim.nonEmpty).map{v =>
        (u.map(_.userName).getOrElse(w.userId) like "%" + v + "%") || (w.windowName like "%" + v + "%")}.getOrElse(true: Rep[Boolean])
    } yield (u.map(_.userName).getOrElse(w.userId), w.windowName, w.count )
  }

  def list(params: WindowSliceTblRequestParams): Future[Seq[WindowCountByUser]] = {
    val q1 = filterQuery(params)
    val q2 = q1.sortBy(v =>
      params.order0Column match {
        case 0 => if (params.order0Dir == "desc") v._1.desc else v._1.asc
        case 1 => if (params.order0Dir == "desc") v._2.desc else v._2.asc
        case 2 => if (params.order0Dir == "desc") v._3.desc else v._3.asc
        case _ => if (params.order0Dir == "desc") v._1.desc else v._1.asc
      }
    )
    val q3 = q2.drop(params.start).take(params.length)
    val f = db.run(q3.result)
    f.map { seq => seq.map{case (userName, windowName, count) =>
      WindowCountByUser(userName, windowName, count)}
    }
  }

}
