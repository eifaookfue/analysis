package jp.co.nri.nefs.tool.analytics.setup.client

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.{DayOfWeek, LocalDate}
import java.time.temporal.ChronoUnit
import java.util.Date

import com.typesafe.config.ConfigFactory
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.common.property.EDestinationType
import jp.co.nri.nefs.tool.analytics.model.client._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.util.{Failure, Random, Success, Try}

trait Initializer {
  def initialize(): Unit
}

class DefaultInitializer @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends Initializer with LogComponent with WindowDetailComponent with PreCheckComponent
    with E9nComponent
  with E9nStackTraceComponent with E9nDetailComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val preChecks = TableQuery[PreChecks]
  val e9ns = TableQuery[E9ns]
  val e9nStackTraces = TableQuery[E9nStackTraces]
  val e9nDetails = TableQuery[E9nDetails]

  override def initialize(): Unit = {

  }

}

object Setup {
  val r: Random = Random
  val appNames: Seq[String] = Seq("TradeSheet", "TradeSheet", "TradeSheet", "BasketSheet", "BasketSheet", "IOISheet")
  val userNames: Seq[String] = Seq("nakamura-s", "miyazaki-m", "saiki-c", "hori-n", "shimizu-r")
  val userIds: Seq[String] = Seq("356431", "356432", "356433", "356434", "356435")
  val windowNames: Seq[String] = Seq("NewOrderSingle", "NewSplit", "NewExecution", "OrderDetail")
  val destinationTypes: Seq[String] = EDestinationType.values.map(_.toString).toSeq
  val actions: Seq[String] = Seq("OK", "OK", "OK", "OK", "CANCEL")
  val e9ns: Seq[String] = Seq("IllegalArgumentException", "RuntimeException", "TimeoutException")

  def fileName(appName: String, env: String, computer: String, userId: String, startTime: Date): String = {
    val format = new SimpleDateFormat("yyyyMMddHHmmssSSS")
    Seq(appName, env, computer, userId, format.format(startTime), "mask.zip").mkString("_")
  }

  def tradeDates(startText: String, endText: String): Seq[String] = {
    val startDate = LocalDate.parse(startText)
    val endDate = LocalDate.parse(endText)
    for {
      diff <- 0 to ChronoUnit.DAYS.between(startDate, endDate).toInt
      date = startDate.plusDays(diff)
      if date.getDayOfWeek != DayOfWeek.SATURDAY
      if date.getDayOfWeek != DayOfWeek.SUNDAY
      str = f"${date.getYear}%04d${date.getMonthValue}%02d${date.getDayOfMonth}%02d"
    } yield str
  }

  def env(appName: String): String = if (appName == "IOISheet") "OMS_TKY_OA" else "OMS_TKY"

  def computerName(userId: String): String = "FID2CAD" + userId.takeRight(3)

  def times(tradeDate: String, startTime: String, endTime: String, count: Int): Seq[Timestamp] = {
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm")
    val start = dateFormat.parse(tradeDate + startTime).getTime
    val end = dateFormat.parse(tradeDate + endTime).getTime
    val interval = (end - start).toInt
    (for {
      _ <- 0 to count
      v = r.nextInt(interval)
      l = start + v
    } yield l).sorted.map(new Timestamp(_))
  }

  def activator(windowName: String): Option[String] = {
    val config = ConfigFactory.load()
    val configKey = "handler-mapping." + windowName
    Try(config.getStringList(configKey).asScala) match {
      case Success(handlerList) =>
        Some(randomValue(handlerList))
      case Failure(_) =>
        None
    }
  }

  def logs(startText: String, endText:String, countPerDay: Int): Seq[Log] = {
    val logId = 0
    val startTextFrom = "0530"
    val startTextTo = "1000"
    for {
      tradeDate <- tradeDates(startText, endText)
      _ <- 0 until countPerDay
      appName = randomValue(appNames)
      ev = env(appName)
      userId = randomValue(userIds)
      cName = computerName(userId)
      time = times(tradeDate, startTextFrom, startTextTo, 1).head
      fName = fileName(appName, ev, cName, userId, time)
      log = Log(logId, appName, cName, userId, tradeDate, time, fName)
    } yield log
  }

  def destinationType(windowName: String): Option[String] =
    if (windowName == "NewSplit") Some(randomValue(destinationTypes)) else None

  def windowDetails(logs: Seq[Log], countPerLog: Int): Seq[WindowDetail] = {
    val dateFormat = new SimpleDateFormat("hhMM")
    for {
      log <- logs
      ts = times(log.tradeDate, dateFormat.format(log.time), "1830", countPerLog)
      lineNo <- 0 until countPerLog
      windowName = randomValue(windowNames)
      act = activator(windowName)
      dest = destinationType(windowName)
      action = randomValue(actions)
      windowDetail = WindowDetail(0, lineNo, act, Some(windowName), dest, Some(action), None, ts(lineNo), Some(r.nextInt(500)))
    } yield windowDetail
  }

  private def randomValue[T](seq: Seq[T]): T = {
    seq(r.nextInt(seq.length))
  }

}
