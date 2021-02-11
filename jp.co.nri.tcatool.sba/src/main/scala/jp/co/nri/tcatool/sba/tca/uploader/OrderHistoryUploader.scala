package jp.co.nri.tcatool.sba.tca.uploader

import java.sql.Timestamp

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.sba.tca.model.{HistoryKey, OrderHistory, OrderHistoryComponent}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.util.Try

class OrderHistoryUploader @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends OrderHistoryComponent with AbstractOrderUploader[OrderHistory]
  with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging {

  import profile.api._

  override val query = TableQuery[OrderHistories]

  override def convert(row: List[String]): Try[OrderHistory] = {

    for {
      historyKey <- toHistoryKey(row)
      historyType <- toHistoryType(row)
      price <- toPrice(row)
      sliceQty <- toSliceQty(row)
      sliceTime <- toSliceTime(row)
      note <- toNote(row)
      brokerCode <- toBrokerCode(row)
      strategyName <- toStrategyName(row, brokerCode)
      ackedTime <- Read[Timestamp].optionalReads(row(44))
      updateTime = new Timestamp(System.currentTimeMillis())
    } yield OrderHistory(historyKey, historyType, price, sliceQty, cumulatives, avgPrice,
      sliceTime, note, strategyName, ackedTime, closedTime, updateTime)
  }

  private def toHistoryKey(row: List[String]): Try[HistoryKey] = {
    for {
      orderKey <- toOrderKey(row)
      orderHistoryNo <- Read[Int].reads(row(4))
    } yield HistoryKey(orderKey, orderHistoryNo)
  }
}

object OrderHistoryUploader extends LazyLogging {

  def main(args: Array[String]): Unit = {
    ServiceInjector.initialize()
    val orderHistoryUploader = ServiceInjector.getComponent(classOf[OrderHistoryUploader])
    orderHistoryUploader.uploadAndMappingInformationSeq.foreach { i =>
      orderHistoryUploader.createMapping(i)
      orderHistoryUploader.upload(i.uploadInformation)
    }
  }

}
