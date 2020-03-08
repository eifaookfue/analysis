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

    val list = for {
      i <- 0 until range.length -1
      q = (for {
        (l, w) <- logs join windowDetails on (_.logId === _.logId)
        if toChar(w.time, format) > range(i) && toChar(w.time, format) <= range(i+1)
      } yield (l, w)).groupBy{ case (l, w) =>
        (l.userId, w.windowName)
      }
      q2 = q.map { case ((userId, windowName), lw) =>
        (range(i) + "_" + range(i+1), userId, windowName.getOrElse("OTHER"), lw.length, 0, 0d)
      }
      selectInsert = windowSlices forceInsertQuery q2
      f = db.run(selectInsert)
    } yield f
    val aggregated: Future[Seq[Int]] = Future.sequence(list)
    Await.result(aggregated, Duration.Inf)

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
    val a = Await.result(agg2, Duration.Inf)
    //val (s1,s2, s3, s4, s5) = a



  }
}

object Analyzer {
  ServiceInjector.initialize()
  val analyzer: Analyzer = ServiceInjector.getComponent(classOf[Analyzer])

  def main(args: Array[String]): Unit = {
    analyzer.analyzeBySlice(LocalTime.of(6, 0), LocalTime.of(17, 0), 10)
  }
}