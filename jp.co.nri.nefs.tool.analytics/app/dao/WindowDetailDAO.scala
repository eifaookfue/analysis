package dao

import java.sql.Timestamp
import java.text.SimpleDateFormat

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.model.client.{LogComponent, WindowDetailComponent}
import jp.co.nri.nefs.tool.analytics.model.common.UserComponent
import models._
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider, SlickModule}
import slick.jdbc.JdbcProfile
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

@Singleton()
class WindowDetailDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, config: Configuration)(implicit executionContext: ExecutionContext)
  extends WindowDetailComponent with LogComponent with UserComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val users = TableQuery[Users]

  // slick.dbs.default
  val dbName: String = config.underlying.getString(SlickModule.DbKeyConfig) +
    "." + config.underlying.getString(SlickModule.DefaultDbName)

  val conf: Config = config.underlying.getConfig(dbName)
  val convertFunction: String = conf.getString("dateToChar.function")
  val toChar: (Rep[Timestamp], Rep[String]) => Rep[String] =
    SimpleFunction.binary[Timestamp, String, String](convertFunction)
  //scala> val list = config.getObjectList("slick.dbs.default.dateFormatters").asScala.map(_.unwrapped).map(_.asScala)
  val dateFormatters: Seq[Map[String, AnyRef]] = conf.getObjectList("dateFormatters").asScala
    .map(_.unwrapped()).map(_.asScala.toMap)

  def count: Future[Int] = {
    db.run(windowDetails.length.result)
  }

  def count(params: WindowDetailTblRequestParams): Future[Int] = {
    db.run(filterQuery(params).length.result)
  }

  private def filterQuery(params: WindowDetailTblRequestParams): Query[(Rep[Int], Rep[String], Rep[String], Rep[Int], Rep[String], Rep[String], Rep[Timestamp]), (Int, String, String, Int, String, String, Timestamp), scala.Seq]
  = {

    val timeStr = params.col6SearchValue
    val formatter = (for {
      o <- dateFormatters
      (simpleFmt, rdbFmt) <- o
      format = new SimpleDateFormat(simpleFmt)
      fmt = Try {
        format.parse(timeStr)
        rdbFmt.toString
      }
    } yield fmt).collectFirst{case Success(v) => v}

    println(s"formatter=$formatter")

    for {
      ((l, w), u) <- logs join windowDetails on (_.logId === _.logId) joinLeft users on (_._1.userId === _.userId)
      if List(
        Option(params.col0SearchValue).filter(_.trim.nonEmpty).map(l.logId === _.toInt),
        Option(params.col1SearchValue).filter(_.trim.nonEmpty).map(l.appName like "%" + _ + "%"),
        Option(params.col2SearchValue).filter(_.trim.nonEmpty).map(u.map(_.userName).getOrElse("") like "%" + _ + "%"),
        Option(params.col3SearchValue).filter(_.trim.nonEmpty).map(w.lineNo === _.toInt),
        Option(params.col4SearchValue).filter(_.trim.nonEmpty).map(w.activator.getOrElse("") like "%" + _ + "%"),
        Option(params.col5SearchValue).filter(_.trim.nonEmpty).map(w.windowName.getOrElse("") like "%" + _ + "%"),
        formatter.map(toChar(w.time, _) === timeStr)
      ).collect ({case Some(criteria) => criteria}).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
    } yield (l.logId, l.appName, u.map(_.userName).getOrElse(l.userId), w.lineNo,
      w.activator.getOrElse(""), w.windowName.getOrElse(""),
    w.time)
  }


  /** Returns a page of WindowDetail */
  def list(params: WindowDetailTblRequestParams): Future[Seq[WindowDetailTbl]] = {
    val q1 = filterQuery(params)
    val q2 = q1.sortBy { case (logId, appName, userName, lineNo, activator, windowName, time) =>
      params.order0Column match {
        case 0 => if (params.order0Dir == "desc") logId.desc else logId.asc
        case 1 => if (params.order0Dir == "desc") appName.desc else appName.asc
        case 2 => if (params.order0Dir == "desc") userName.desc else userName.asc
        case 3 => if (params.order0Dir == "desc") lineNo.desc else lineNo.asc
        case 4 => if (params.order0Dir == "desc") activator.desc else activator.asc
        case 5 => if (params.order0Dir == "desc") windowName.desc else windowName.asc
        case 6 => if (params.order0Dir == "desc") time.desc else time.asc
        case _ => if (params.order0Dir == "desc") logId.desc else logId.asc
      }
    }
    val q3 = q2.drop(params.start).take(params.length)
    val f = db.run(q3.result)
    f.map(seq => seq.map{case (logId, appName, userName, lineNo, activator, windowName, time) =>
      WindowDetailTbl(logId, appName, userName, lineNo, activator, windowName, time)
    })
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

  def fileName(logId: Int): Future[(String, String)] = {
    val query = logs.filter(_.logId === logId).map(l => (l.tradeDate, l.fileName))
    val action = query.result.head
    db.run(action)
  }

}
