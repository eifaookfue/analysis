package jp.co.nri.nefs.tool.analytics.generator.client

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.{DayOfWeek, LocalDate}
import java.time.temporal.ChronoUnit
import java.util.Date

import com.typesafe.config.ConfigFactory
import jp.co.nri.nefs.tool.analytics.common.property.EDestinationType
import jp.co.nri.nefs.tool.analytics.model.client._
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Random, Success, Try}

object Generator {
  val r: Random = Random
  val appNames: Seq[String] = Seq("TradeSheet", "TradeSheet", "TradeSheet", "BasketSheet", "BasketSheet", "IOISheet")
  val userNames: Seq[String] = Seq("nakamura-s", "miyazaki-m", "saiki-c", "hori-n", "shimizu-r")
  val userIds: Seq[String] = Seq("356431", "356432", "356433", "356434", "356435")
  val windowNames: Seq[String] = Seq("NewOrderSingle", "NewSplit", "NewExecution", "OrderDetail")
  val destinationTypes: Seq[String] = EDestinationType.values.map(_.toString).toSeq
  val actions: Seq[String] = Seq("OK", "OK", "OK", "OK", "CANCEL")
  val e9ns: Seq[String] = Seq("IllegalArgumentException", "RuntimeException", "TimeoutException")
  val messages: Seq[String] = Seq("対象レコードがありません。", "株数が入力されていません。")

  def main(args: Array[String]): Unit = {
    ServiceInjector.initialize()
    val recorder = ServiceInjector.getComponent(classOf[ClientLogRecorder])
    recorder.recreate()
    val sequence = 1 to 10
    var e9nId = 0
    for {
      log <- Generator.logs("2019-01-01", "2019-01-31", 10)
      logId = recorder.record(log)
      windowDetail <- Generator.windowDetails(log, 20)
      f = recorder.record(logId.get, windowDetail)
      _ = Await.ready(f, Duration.Inf)
      // record e9n once every 10 times

      _ = if (randomValue(sequence) == 1) {
        val f1 = recorder.recordE9n(logId.get, windowDetail.lineNo, new Timestamp(System.currentTimeMillis()), e9nStaceTraceSeq(10))
        Await.ready(f1, Duration.Inf)
        val f2 = recorder.record(E9nAudit(e9nId, STATUS.DONE, None, "nakamura-s"))
        Await.ready(f2, Duration.Inf)
        e9nId = e9nId + 1
      }
      check <- preCheck(logId.get, windowDetail, 0.25)
      f3 = recorder.record(check)
      _ = Await.ready(f3, Duration.Inf)
      _ = f3.value.get match {
        case Success(_) => println("Succeeded inserting PreCheck!")
        case Failure(e) => println(e)
      }
    } {}
  }

  def e9nLists(count: Int): Seq[E9n] = {
    for {
      i <- 0 until count
      message = randomValue(e9ns)
      e9n = E9n(0, message, i)
    } yield e9n
  }

  def e9nStaceTraceSeq(count: Int): Seq[E9nStackTrace] = {
    for {
      i <- 0 until count
      e9nStackTrace = E9nStackTrace(0, i, message(i))
    } yield e9nStackTrace
  }

  private def message(number: Int): String = if (number == 0) randomValue(e9ns) else s"at $number"

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

  def windowDetails(log: Log, countPerLog: Int): Seq[WindowDetail] = {
    val dateFormat = new SimpleDateFormat("hhMM")
    val ts = times(log.tradeDate, dateFormat.format(log.time), "1830", countPerLog)
    for {
      lineNo <- 1 to countPerLog
      windowName = randomValue(windowNames)
      act = activator(windowName)
      dest = destinationType(windowName)
      action = randomValue(actions)
      windowDetail = WindowDetail(0, lineNo, act, Some(windowName), dest, Some(action), None, ts(lineNo), Some(r.nextInt(500)))
    } yield windowDetail
  }

  def preCheck(logId: Int, detail: WindowDetail, probOccurrence: Double): Option[PreCheck] = {
    val limit = math.floor(1 / probOccurrence).toInt
    val sequence = 1 to limit
    if (randomValue(sequence) == 1) {
      val message = randomValue(messages)
      val code = s"Code${messages.indexOf(message)}"
      Some(PreCheck(logId, detail.lineNo, detail.windowName, code, message))
    } else None
  }

  private def randomValue[T](seq: Seq[T]): T = {
    seq(r.nextInt(seq.length))
  }

}
