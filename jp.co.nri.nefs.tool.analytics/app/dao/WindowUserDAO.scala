package dao

import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.model.client.WindowUserComponent
import jp.co.nri.nefs.tool.analytics.model.common.UserComponent
import models.{WindowCountByUser, WindowCountTableParams}
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

  def count(params: WindowCountTableParams): Future[Int] = {
    val q = for {
      (w, u) <- windowUsers joinLeft users on (_.userId === _.userId)
    } yield (u.map(_.userName).getOrElse(""), w.windowName, w.count)
    val q2 = q.filter { case (userName, windowName, _) =>
      (userName like "%" + params.searchValue + "%") || (windowName like "%" + params.searchValue + "%")
    }
    db.run(q2.length.result)
  }

  def list(params: WindowCountTableParams): Future[Seq[WindowCountByUser]] = {
    val q = for {
      (w, u) <- windowUsers joinLeft users on (_.userId === _.userId)
    } yield (u.map(_.userName).getOrElse(""), w.windowName, w.count)
    val q2 = q.filter { case (userName, windowName, _) =>
      (userName like "%" + params.searchValue + "%") || (windowName like "%" + params.searchValue + "%")
    }
    val q3 = if (params.order0Column == 0 && params.order0Dir == "desc") {
      q2.sortBy{case (u, _, _) => u.desc}
    } else if (params.order0Column == 0 && params.order0Dir == "asc") {
      q2.sortBy{case (u, _, _) => u.asc}
    } else if (params.order0Column == 1 && params.order0Dir == "desc") {
      q2.sortBy{case (_, w, _) => w.desc}
    } else if (params.order0Column == 1 && params.order0Dir == "asc") {
      q2.sortBy{case (_, w, _) => w.asc}
    } else if (params.order0Column == 2 && params.order0Dir == "desc") {
      q2.sortBy{case (_, _, c) => c.desc}
    } else {
      q2.sortBy{case (_, _, c) => c.asc}
    }
    val q4 = q3.drop(params.start).take(params.length)
    val fut = db.run(q4.result)
    fut.map{ seq =>
      for {
        (userName, windowName, count) <- seq
        w = WindowCountByUser(userName, windowName, count)
      } yield w
    }
  }

}
