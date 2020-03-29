package jp.co.nri.nefs.tool.analytics.analyze.client

import java.sql.Timestamp
import java.time.LocalTime
import java.time.temporal.ChronoUnit

import com.typesafe.config.{Config, ConfigFactory}
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.client.{LogComponent, WindowDetailComponent, WindowSliceComponent}
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class Analyzer @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends LogComponent with WindowDetailComponent with WindowSliceComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val windowSlices = TableQuery[WindowSlices]


  /** startTimeからendTimeまでintervalMinutes毎に時刻(HHMM)を算出します。
    * 先頭には"0000"、末尾には"2400"を自動で挿入します。
   */
  def timeRange(startTime: LocalTime, endTime: LocalTime, intervalMinutes: Int): Seq[String] = {
    val mid = for {
      diff <- 0 to ChronoUnit.MINUTES.between(startTime, endTime).toInt by intervalMinutes
      calcTime = startTime.plus(java.time.Duration.ofMinutes(diff))
      str = "%02d".format(calcTime.getHour) + "%02d".format(calcTime.getMinute)
    } yield str
    val buffer = ListBuffer[String]()
    buffer += "0000"
    buffer ++= mid
    buffer += "2400"
    buffer
  }

  val config: Config = ConfigFactory.load()
  val conf: Config = config.getConfig(config.getString("play.slick.db.default"))
  val fname: String = conf.getString("dateToChar.function")
  val format: String = conf.getString("dateToChar.format")

  val toChar: (Rep[Timestamp], Rep[String]) => Rep[String] =
    SimpleFunction.binary[java.sql.Timestamp, String, String](fname)

  def analyzeBySlice(startTime: LocalTime, endTime: LocalTime, intervalMinutes: Int): Unit = {
    val range = timeRange(startTime, endTime, intervalMinutes)

    val setup = DBIO.seq(
      windowSlices.schema.drop,
      windowSlices.schema.create,
    )
    val f1 = db.run(setup)
    Await.result(f1, Duration.Inf)

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
        (range(i) + "_" + range(i+1), userId, windowName.getOrElse("OTHER"), lw.length, 0, 0l)
      }
      selectInsert = windowSlices forceInsertQuery q2
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
        (range(i) + "_" + range(i + 1), userId, windowName.getOrElse("OTHER"), lw.length, lw.map(_._2.startupTime).avg)
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
      q = windowSlices.filter(_.slice === slice).filter(_.userId === userId).filter(_.windowName === windowName)
        .map(s => (s.startupCount, s.avgStartup))
      updateAction = q.update((count, avgStartupOp.getOrElse(0l)))
      f = db.run(updateAction)
    } yield f
    val agg3 = Future.sequence(list3)
    Await.result(agg3, Duration.Inf)
  }
}

object Analyzer {
  ServiceInjector.initialize()
  val analyzer: Analyzer = ServiceInjector.getComponent(classOf[Analyzer])

  def main(args: Array[String]): Unit = {
    analyzer.analyzeBySlice(LocalTime.of(6, 0), LocalTime.of(17, 0), 10)
  }
}