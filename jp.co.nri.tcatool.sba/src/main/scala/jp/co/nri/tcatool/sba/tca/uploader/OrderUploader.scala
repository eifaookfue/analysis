package jp.co.nri.tcatool.sba.tca.uploader

import java.sql.Timestamp
import java.time.LocalDate

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.ref.reference.model.{EBSType, EMarket, ESessionType}
import jp.co.nri.tcatool.sba.model.EHistoryType
import jp.co.nri.tcatool.sba.tca.model.{Order, OrderBase, OrderComponent}
import play.api.db.slick.DatabaseConfigProvider

import scala.util.Try

class OrderUploader @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends OrderComponent with AbstractOrderUploader[Order] with LazyLogging {

  import profile.api._

  override val query = TableQuery[Orders]

  override def convert(row: List[String]): Try[Order] = {

    import Read._
    import jp.co.nri.tcatool.sba.tca.read.SBARead._

    for {
      orderKey <- toOrderKey(row)
      baseDate <- Read[LocalDate].reads(row(2))
      sessionType <- Read[ESessionType].reads(row(19))
      symbol <- Read[String].reads(row(25)).map(symbolMap(_))
      lot <- Read[Int].reads(row(29))
      bsType <- Read[EBSType].reads(row(30))
      orderQty <- Read[BigDecimal].reads(row(31)).map(_.toInt)
      price <- toPrice(row)
      sliceQty <- toSliceQty(row)
      market <- Read[EMarket].optionalReads(row(36))
      brokerCode <- toBrokerCode(row)
      brokerId <- Try(brokerMap(brokerCode))
      sliceDate <- Read[LocalDate].reads(row(38))
      sliceTime <- toSliceTime(row)
      note <- toNote(row)
      strategyName <- toStrategyName(row, brokerCode)
      orderBase = OrderBase(orderKey, baseDate, sessionType, symbol, lot, bsType, orderQty, price,
        sliceQty, market, brokerId, sliceDate, sliceTime, note, strategyName)
      (basePrice, orderAmount, volume, changeRatio, spread) = (None, None, None, None, None)
      updateTime = new Timestamp(System.currentTimeMillis())
    } yield Order(orderBase, cumulatives, basePrice, orderAmount, volume, avgPrice, benchMark,
      closedTime, changeRatio, spread, updateTime)
  }

  override def condition(row: List[String]): Boolean = {

    val historyType = toHistoryType(row)
    // Collect only HistoryType == New
    historyType.map(_ == EHistoryType.NEW).getOrElse(false)

  }

}

object OrderUploader extends LazyLogging {

  def main(args: Array[String]): Unit = {

    try {
      ServiceInjector.initialize()
      val orderUploader = ServiceInjector.getComponent(classOf[OrderUploader])

      orderUploader.uploadAndMappingInformationSeq.foreach { i =>
        logger.info(s"$i execution started.")
        orderUploader.createMapping(i)
        orderUploader.upload(i.uploadInformation)
        logger.info(s"$i execution ended.")
      }
    } catch {
      case e: Exception => logger.error("Error occurred.", e)
    }

  }

}

