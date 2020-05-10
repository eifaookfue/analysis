package dao

import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.model.client.{Log, LogComponent, WindowDetail, WindowDetailComponent}
import jp.co.nri.nefs.tool.analytics.model.common.{User, UserComponent}
import models._
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class WindowDetailDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, config: Configuration)(implicit executionContext: ExecutionContext)
  extends WindowDetailComponent with LogComponent with UserComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val users = TableQuery[Users]

  /** Count all computers. */
  def count(): Future[Int] = {
    // this should be changed to
    // db.run(computers.length.result)
    // when https://github.com/slick/slick/issues/1237 is fixed
    db.run(windowDetails.map(_.windowName).length.result)
  }
  /** Count computers with a filter. */
  def count(filterHandler: Option[String], filterWindowName: Option[String]): Future[Int] = {
    db.run(windowDetails.filter { windowDetail => windowDetail.windowName.toLowerCase like filterHandler.map(_.toLowerCase).getOrElse("") }.length.result)
  }


  /** Return a page of WindowDetail */
  def list(): Future[Seq[WindowDetailTable]] = {
    val q1 = logs join windowDetails on (_.logId === _.logId) joinLeft users on (_._1.userId === _.userId)
    val q2 = q1.take(10)
    val f = db.run(q2.result)
    f.map{ seq =>
      for {
        ((l, w), u) <- seq
        windowDetailTable = WindowDetailTable(l.logId, l.appName, l.computerName, u.map(_.userName).getOrElse(""),
          l.tradeDate, w.lineNo, w.activator.getOrElse(""), w.windowName.getOrElse(""), w.destinationType.getOrElse(""),
          w.action.getOrElse(""), w.method.getOrElse(""), w.time, w.startupTime)
      } yield windowDetailTable
    }
  }

  /** Returns the sequence of WindowCountByDate object <br>
    * SQL image:
    * {{{
    * SELECT
    *   l.TRADE_DATE, w.WINDOW_NAME, COUNT(1)
    * FROM
    *   LOG l, WINDOW_DETAIL w
    * WHERE
    *   l.LOG_ID = w.LOG_ID
    * GROUP BY
    *   l.TRADE_DATE, w.WINDOW_NAME
    * ORDER BY
    *   l.TRADE_DATE, w.WINDOW_NAME
    * }}}
    * @return Sequence of WindowCountByDate
    */
  def windowCountByDate: Future[Seq[WindowCountByDate]] = {
    val q = (for {
      (l, w) <- logs join windowDetails on (_.logId === _.logId)
    } yield (l, w)).groupBy { case (l, w) => (l.tradeDate, w.windowName)}
    val q2 = q.map { case ((tradeDate, windowName), lw) =>
      (tradeDate, windowName.getOrElse(""), lw.length)
    }.sortBy { case (logId, windowName, _) => (logId, windowName)}
    val fut = db.run(q2.result)
    val groupFut = fut.map(_.groupBy(_._1))
    groupFut.map { fut =>
      (for {
        (tradeDate, seq) <- fut
        total = seq.map(_._3).sum
        nos = seq.filter(_._2.contains("NewOrder")).map(_._3).sum
        ns = seq.filter(_._2.contains("NewSplit")).map(_._3).sum
        ws = WindowCountByDate(tradeDate, nos, ns, total - nos - ns)
      } yield ws).toSeq
    }
  }

  /** Returns a sequence of WindowCountByUser object <br>
    * SQL image:
    * {{{
    *   SELECT
    *     u.USER_NAME, w.WINDOW_NAME, COUNT(1)
    *   FROM
    *     WINDOW_DETAIL w, LOG l, USER u
    *   WHERE
    *     w.LOG_ID = l.LOG_ID
    *     AND l.USER_ID = u.USER_ID
    *   GROUP BY
    *     u.USER_NAME, w.WINDOW_NAME
    *   ORDER BY
    *     COUNT DESC
    * }}}
    * @return A sequence of WindowCountByUser object
    */
  def windowCountByUser: Future[Seq[WindowCountByUser]] = {
    val q = for {
      (w, l) <- windowDetails join logs on (_.logId === _.logId)
    } yield (w, l)
    val q2 = (for {
      ((w, _), u) <- q joinLeft users on (_._2.userId === _.userId)
    } yield (w, u)).groupBy{ case (w, u) => (u.map(_.userName).getOrElse(""), w.windowName.getOrElse(""))}
    val q3 = q2.map { case ((userName, windowName), uw) => (userName, windowName, uw.length)}
      .sortBy {case (_, _, count) => count.desc }

    val fut = db.run(q3.result)
    fut.map { seq =>
      for ((userName, windowName, count) <- seq) yield WindowCountByUser(userName, windowName, count)
    }

  }

  def fileName(logId: Int): Future[String] = {
    val query = logs.filter(_.logId === logId).map(_.fileName)
    val action = query.result.head
    db.run(action)
  }

}
