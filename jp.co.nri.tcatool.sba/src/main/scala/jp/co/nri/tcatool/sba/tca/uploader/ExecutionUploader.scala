package jp.co.nri.tcatool.sba.tca.uploader

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime, LocalTime}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.common.upload.{DBUtil, SimpleUploader, Uploaders}
import jp.co.nri.tcatool.ref.reference.model.EMarket
import jp.co.nri.tcatool.sba.tca.model.{Execution, ExecutionComponent, HistoryKey, OrderKey}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.util.Try


class ExecutionUploader @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends ExecutionComponent with SimpleUploader[Execution] with DBUtil[Execution]
    with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging {


  import profile.api._

  override val query = TableQuery[Executions]

  override def convert(row: List[String]): Try[Execution] = {

    import Read._
    import jp.co.nri.tcatool.sba.tca.read.SBARead._

    for {
      executionId <- Read[String].reads(row(3))
      historyKey <- toHistoryKey(row)
      price <- Read[BigDecimal].reads(row(30))
      execQty <- Read[BigDecimal].reads(row(31)).map(_.toInt)
      executionTime <- toExecutionTime(row) // date includes white space
      market <- Read[EMarket].reads(row(32))
      note <- Read[String].optionalReads(row(54))
      updateTime = new Timestamp(System.currentTimeMillis())
    } yield Execution(executionId, historyKey, price, execQty, executionTime, market, note, updateTime)
  }

  def toOrderKey(row: List[String]): Try[OrderKey] = {
    for {
      compId <- Read[String].reads(row(1))
      orderId <- Read[String].reads(row(12))
    } yield OrderKey(compId, orderId)
  }

  def toHistoryKey(row: List[String]): Try[HistoryKey] = {
    for {
      orderKey <- toOrderKey(row)
      historyNo <- Read[Int].reads(row(13))
    } yield HistoryKey(orderKey, historyNo)
  }

  def toExecutionTime(row: List[String]): Try[Timestamp] = {
    for {
      date <- Read[LocalDate].reads(row(41))
      time <- Read[LocalTime].reads(row(40))
      dateTime = LocalDateTime.of(date, time)
    } yield Timestamp.valueOf(dateTime)
  }

}

object ExecutionUploader extends LazyLogging{

  final val CONFIG_BASE = "ExecutionUploader"

  def main(args: Array[String]): Unit = {

    ServiceInjector.initialize()
    val executionUploader = ServiceInjector.getComponent(classOf[ExecutionUploader])

    Uploaders.uploadInformationSeq(ConfigFactory.load().getConfig(CONFIG_BASE))
      .foreach(executionUploader.upload)

  }
}
