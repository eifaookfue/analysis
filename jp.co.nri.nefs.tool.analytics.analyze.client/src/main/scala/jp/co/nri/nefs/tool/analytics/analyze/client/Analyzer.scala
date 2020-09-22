package jp.co.nri.nefs.tool.analytics.analyze.client

import java.sql.Timestamp
import java.time.LocalTime
import java.time.temporal.ChronoUnit

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.client._
import jp.co.nri.nefs.tool.analytics.model.common.UserComponent
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider, SlickModule}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class Analyzer @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends LogComponent with WindowDetailComponent with WindowUserSliceComponent
  with UserComponent with WindowDateComponent with WindowSliceComponent
    with WindowUserComponent with PreCheckComponent with PreCheckSummaryComponent with LazyLogging
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val windowDates = TableQuery[WindowDates]
  val windowSlices = TableQuery[WindowSlices]
  val windowUserSlices = TableQuery[WindowUserSlices]
  val windowUsers = TableQuery[WindowUsers]
  val users = TableQuery[Users]
  val preChecks = TableQuery[PreChecks]
  val preCheckSummaries = TableQuery[PreCheckSummaries]

  /** Returns a sequence of Time String from startTime to endTime by intervalMinutes
   */
  def timeRange(startTime: LocalTime, endTime: LocalTime, intervalMinutes: Int): Seq[String] = {
    for {
      diff <- 0 to ChronoUnit.MINUTES.between(startTime, endTime).toInt by intervalMinutes
      calcTime = startTime.plus(java.time.Duration.ofMinutes(diff))
      str = "%02d".format(calcTime.getHour) + "%02d".format(calcTime.getMinute)
    } yield str
  }

  val config: Config = ConfigFactory.load()

  // slick.dbs.default
  val dbName: String = config.getString(SlickModule.DbKeyConfig) +
    "." + config.getString(SlickModule.DefaultDbName)

  val conf: Config = config.getConfig(dbName)
  val convertFunction: String = conf.getString("dateToChar.function")
  val format: String = conf.getString("dateToChar.hourMinutesFormat")

  val toChar: (Rep[Timestamp], Rep[String]) => Rep[String] =
    SimpleFunction.binary[java.sql.Timestamp, String, String](convertFunction)

  /** Returns the sequence of WindowCountByDate object <br>
    * SQL image:
    * {{{
    * INSERT INTO WINDOW_NAME, COUNT(1) (
    *   SELECT
    *     l.TRADE_DATE, w.WINDOW_NAME, COUNT(1)
    *   FROM
    *     LOG l, WINDOW_DETAIL w
    *   WHERE
    *     l.LOG_ID = w.LOG_ID
    *   GROUP BY
    *     l.TRADE_DATE, w.WINDOW_NAME
    *   ORDER BY
    *     l.TRADE_DATE, w.WINDOW_NAME
    * )
    * }}}
    * @return Sequence of WindowCountByDate
    */
  def analyzeByDate(): Unit = {
    val drop = windowDates.schema.dropIfExists
    val f1 = db.run(drop)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(_) => logger.info("drop succeeded.")
      case Failure(e) => logger.error("drop failed.", e)
    }

    val create = windowDates.schema.createIfNotExists
    val f2 = db.run(create)
    Await.ready(f2, Duration.Inf)

    val q = (for {
      (l, w) <- logs join windowDetails on (_.logId === _.logId)
    } yield (l, w)).groupBy { case (l, w) => (l.tradeDate, w.windowName)}
    val q2 = q.map { case ((tradeDate, windowName), lw) =>
      (tradeDate, windowName, lw.length)
    }.sortBy { case (tradeDate, windowName, _) => (tradeDate, windowName)}
    val tradeWindowCountListFut = db.run(q2.result)
    val tradeWindowNameListFut = tradeWindowCountListFut.map(fut => fut.map{ case (tradeDate, windowNameOp, count) =>
      (tradeDate, windowNameOp.map { windowName =>
        if (windowName.contains("NewOrder")) "NewOrder"
        else if (windowName.contains("NewSplit")) "NewSplit"
        else "Other"
      }.getOrElse("Other"), count)
    })
    val windowDateListFut = tradeWindowNameListFut.map { fut =>
      fut.groupBy{ case (tradeDate, windowName, _) =>
        (tradeDate, windowName)
      }
    }.map { fut =>
      fut.map{ case ((tradeDate, windowName), seq) =>
        WindowDate(tradeDate, windowName, seq.map(_._3).sum)
      }
    }

    val future = windowDateListFut.flatMap { windowDateList =>
      val insert = windowDates ++= windowDateList
      db.run(insert)
    }

    Await.ready(future, Duration.Inf)
    future.value.get match {
      case Success(_) => logger.info("insert completed.")
      case Failure(e) => logger.error("insert failed.", e)
    }
  }

  def analyzeBySlice(startTime: LocalTime, endTime: LocalTime, intervalMinutes: Int): Unit = {
    val range = timeRange(startTime, endTime, intervalMinutes)

    val drop = windowUserSlices.schema.dropIfExists
    val f1 = db.run(drop)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(_) => logger.info("drop succeeded.")
      case Failure(e) => logger.error("drop failed", e)
    }

    val create = windowUserSlices.schema.createIfNotExists
    val f2 = db.run(create)
    Await.ready(f2, Duration.Inf)

    val list1 = for {
      i <- 0 until range.length - 1
      q = windowDetails.filter(w => toChar(w.time, format) > range(i) && toChar(w.time, format) <= range(i+1))
        .groupBy(_.windowName).map{ case (windowName, windowAgg) => (range(i), windowName, windowAgg.length)}
      f = db.run(q.result)
    } yield f
    val futures1 = Future.sequence(list1)
    val sliceWindowCountList = Await.result(futures1, Duration.Inf).flatten
    val sliceNameCountList = sliceWindowCountList.map { case (slice, op, count) =>
      (slice, op.map { windowName =>
        if (windowName.contains("NewOrder")) "NewOrder"
        else if (windowName.contains("NewSplit")) "NewSplit"
        else "Other"
      }.getOrElse("Other"), count)
    }.groupBy{ case (slice, windowName, _) =>
      (slice, windowName)
    }
    val sliceNameCount = sliceNameCountList.map { case ((slice, windowName), seq) =>
      ((slice, windowName), seq.map(_._3).sum)
    }
    val list2 = for {
      ((slice, windowName), count) <- sliceNameCount
      windowSlice = WindowSlice(slice, windowName, count)
      insert = windowSlices += windowSlice
      f = db.run(insert)
    } yield f
    val futures2 = Future.sequence(list2)
    Await.ready(futures2, Duration.Inf)
    futures2.value.get match {
      case Success(_) => logger.info("insert completed.")
      case Failure(e) => logger.error("insert failed.", e)
    }

  }

  def analyzeByUserSlice(startTime: LocalTime, endTime: LocalTime, intervalMinutes: Int): Unit = {
    val range = timeRange(startTime, endTime, intervalMinutes)

    val drop = windowUserSlices.schema.dropIfExists
    val f1 = db.run(drop)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(_) => logger.info("drop succeeded.")
      case Failure(e) => logger.error("drop failed", e)
    }

    val create = windowUserSlices.schema.createIfNotExists
    val f2 = db.run(create)
    Await.ready(f2, Duration.Inf)


    /*
      INSERT INTO WINDOW_SLICE(
	      SELECT '0900', l.USER_ID, NVL(WINDOW_NAME,'OTHER'), COUNT(1), 0, 0
	      FROM
		      LOG l INNER JOIN WINDOW_DETAIL w
		      ON l.LOG_ID = w.LOG_ID
	      WHERE
		    to_char(w.TIME, 'HH24MI') > '0000' AND to_char(w.TIME, 'HH24MI') < '0900'
      )
     */
    val list = for {
      i <- 0 until range.length -1
      q = (for {
        (l, w) <- logs join windowDetails on (_.logId === _.logId)
        if toChar(w.time, format) > range(i) && toChar(w.time, format) <= range(i+1)
      } yield (l, w)).groupBy{ case (l, w) =>
        (l.userId, w.windowName)
      }
      q2 = q.map { case ((userId, windowName), lw) =>
        (range(i), userId, windowName.getOrElse("OTHER"), lw.length, 0, 0l)
      }
      selectInsert = windowUserSlices forceInsertQuery q2
      f = db.run(selectInsert)
    } yield f
    val aggregated: Future[Seq[Int]] = Future.sequence(list)
    Await.result(aggregated, Duration.Inf)

    /*
      updateActionで利用するため、slice、userId、windowNameをキー、
      カウント、startupTime平均値のテーブルを算出する

      SELECT '0900', l.USER_ID, NVL(WINDOW_NAME,'OTHER'), COUNT(1), AVG(STARTUP_TIME)
      FROM
	      LOG l INNER JOIN WINDOW_DETAIL w
	      ON l.LOG_ID = w.LOG_ID
      WHERE
	      to_char(w.TIME, 'HH24MI') > '0000' AND to_char(w.TIME, 'HH24MI') < '0900'
	      AND w.STARTUP_TIME is not null
      GROUP BY
	      l.USER_ID, w.WINDOW_NAME
     */
    val list2 = for {
      i <- 0 until range.length - 1
      q = (for {
        (l, w) <- logs join windowDetails on (_.logId === _.logId)
        if toChar(w.time, format) > range(i) && toChar(w.time, format) <= range(i + 1)
        if w.startupTime.isDefined
      } yield (l, w)).groupBy { case (l, w) =>
        (l.userId, w.windowName)
      }
      q2 = q.map { case ((userId, windowName), lw) =>
        (range(i), userId, windowName.getOrElse("OTHER"), lw.length, lw.map(_._2.startupTime).avg)
      }
      f = db.run(q2.result)
    } yield  f

    val agg2 = Future.sequence(list2)
    val sliceList = Await.result(agg2, Duration.Inf)

    /*
      UPDATE WINDOW_SLICE
        SET STARTUP_COUNT = ?, AVG_STARTUP = ?
      WHERE
	      SLICE = '0900'
        AND USER_ID = 'user-A'
        AND WINDOW_NAME = 'NewSplit'
     */
    // slice毎のList。select結果は複数ありえる（今回はないが）のでそれもList
    val list3 = for {
      tmpSlice <- sliceList
      (slice, userId, windowName, count, avgStartupOp) <- tmpSlice
      q = windowUserSlices.filter(_.slice === slice).filter(_.userId === userId).filter(_.windowName === windowName)
        .map(s => (s.startupCount, s.avgStartup))
      updateAction = q.update((count, avgStartupOp.getOrElse(0l)))
      f = db.run(updateAction)
    } yield f
    val agg3 = Future.sequence(list3)
    Await.result(agg3, Duration.Inf)
  }

  /** Insert into WINDOW_USER by using results of WINDOW_DETAIL aggregation. <br>
    * SQL image:
    * {{{
    *   INSERT INTO WINDOW_USER(
    *     SELECT
    *       l.USER_ID, w.WINDOW_NAME, COUNT(1)
    *     FROM
    *       WINDOW_DETAIL w, LOG l
    *     WHERE
    *       w.LOG_ID = l.LOG_ID
    *     GROUP BY
    *       u.USER_ID, w.WINDOW_NAME
    *     ORDER BY
    *       COUNT DESC
    *   )
    * }}}
    */
  def analyzeByUser(): Unit = {

    val drop = windowUsers.schema.dropIfExists
    val f1 = db.run(drop)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(_) => logger.info("drop succeeded.")
      case Failure(e) => logger.error("drop failed", e)
    }

    val create = windowUsers.schema.createIfNotExists
    val f2 = db.run(create)
    Await.ready(f2, Duration.Inf)

    val q = for {
      (w, l) <- windowDetails join logs on (_.logId === _.logId)
    } yield (w, l)
    val q2 = q.groupBy{ case (w, l) => (l.userId, w.windowName.getOrElse(""))}
    val q3 = q2.map { case ((userId, windowName), uw) => (userId, windowName, uw.length)}
      .sortBy {case (_, _, count) => count.desc }
    val fut = db.run(q3.result)
    val windowCountByUsers = Await.result(fut, Duration.Inf).map { case (userId, windowName, count) =>
      WindowUser(userId, windowName, count)
    }
    val insert = windowUsers ++= windowCountByUsers
    Await.ready(db.run(insert), Duration.Inf)
  }

  def analyzePreCheck(): Unit = {

    val drop = preCheckSummaries.schema.dropIfExists
    val f1 = db.run(drop)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(_) => logger.info(s"Drop PRE_CHECK_SUMMARY succeeded.")
      case Failure(e) => logger.error(s"Drop PRE_CHECK_SUMMARY failed", e)
    }

    val create = preCheckSummaries.schema.createIfNotExists
    val f2 = db.run(create)
    Await.ready(f2, Duration.Inf)

    val q = preChecks.groupBy { p => (p.message, p.windowName.getOrElse(""))}
      .map{case ((message, windowName), mw) => (message , windowName, mw.length)}
      .sortBy{case (message, windowName, _) => (message, windowName)}

    val f3 = db.run(q.result)
    Await.ready(f3, Duration.Inf)
    val preCheckSummariesSeq = f3.value.get match {
      case Success(v) => v.map{ case (message, windowName, count) => PreCheckSummary(message, windowName, count)}
      case Failure(e) => throw e
    }

    val insertPreCheckSummary = preCheckSummaries ++= preCheckSummariesSeq

    val f4 = db.run(insertPreCheckSummary)
    Await.ready(f4, Duration.Inf)
    f4.value.get match {
      case Success(_) => logger.info(s"Insert PRE_CHECK_SUMMARY succeeded.")
      case Failure(e) => logger.error(s"Insert PRE_CHECK_SUMMARY failed.", e)
    }

  }


  }

object Analyzer {
  ServiceInjector.initialize()
  val analyzer: Analyzer = ServiceInjector.getComponent(classOf[Analyzer])

  def main(args: Array[String]): Unit = {
    analyzer.analyzeBySlice(LocalTime.of(5, 0), LocalTime.of(17, 0), 10)
    analyzer.analyzeByUserSlice(LocalTime.of(5, 0), LocalTime.of(17, 0), 10)
    analyzer.analyzeByUser()
    analyzer.analyzeByDate()
    analyzer.analyzePreCheck()
  }
}