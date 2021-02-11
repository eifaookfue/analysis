package jp.co.nri.tcatool.ref.alpha.marketdata.uploader

import java.sql.Timestamp
import java.time.LocalDate

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.common.upload.{DBUtil, SimpleUploader, Uploaders}
import jp.co.nri.tcatool.ref.alpha.common.model.AvgQuoteVolume
import jp.co.nri.tcatool.ref.alpha.marketdata.model._
import jp.co.nri.tcatool.ref.reference.model.EMarket
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.util.Try

class MarketDataPerDayUploader @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends MarketDataPerDayComponent with SimpleUploader[MarketDataPerDay] with DBUtil[MarketDataPerDay]
  with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging {

  import profile.api._

  val query = TableQuery[MarketDataPerDays]

  override def convert(row: List[String]): Try[MarketDataPerDay] = {

    import jp.co.nri.tcatool.ref.alpha.common.read.AlphaRead._

    for {
      tradeDate <- Read[LocalDate].reads(row.head)
      symbol <- Read[String].reads(row(1))
      market <- Read[EMarket].optionalReads(row(2))
      open <- Read[BigDecimal].optionalReads(row(3))
      high <- Read[BigDecimal].optionalReads(row(4))
      low <- Read[BigDecimal].optionalReads(row(5))
      close <- Read[BigDecimal].optionalReads(row(6))
      basePrice = BasePrice(open, high, low, close)
      volume <- Read[BigDecimal].optionalReads(row(7)).map(_.map(_.toInt))
      adjFactor <- Read[BigDecimal].optionalReads(row(10))
      adjValuePrice <- Read[BigDecimal].optionalReads(row(11))
      lastValuePrice <- Read[BigDecimal].optionalReads(row(12))
      adjVolume <- Read[BigDecimal].optionalReads(row(13))
      dayVwap <- Read[BigDecimal].optionalReads(row(14))
      amVwap <- Read[BigDecimal].optionalReads(row(15))
      pmVwap <- Read[BigDecimal].optionalReads(row(16))
      vwap = VWAP(dayVwap, amVwap, pmVwap)
      tradeAmount <- Read[BigDecimal].optionalReads(row(17))
      listingVolume <- Read[BigDecimal].optionalReads(row(18))
      avgVolume5 <- Read[BigDecimal].optionalReads(row(23))
      avgVolume10 <- Read[BigDecimal].optionalReads(row(24))
      avgVolume = AvgVolume(avgVolume5, avgVolume10)
      tickCount <- Read[BigDecimal].optionalReads(row(25)).map(_.map(_.toInt))
      askAvgQuoteVolume <- Read[BigDecimal].optionalReads(row(26))
      bidAvgQuoteVolume <- Read[BigDecimal].optionalReads(row(27))
      avgQuoteVolume = AvgQuoteVolume(askAvgQuoteVolume, bidAvgQuoteVolume)
      mainMarket <- Read[EMarket].optionalReads(row(29))
      updateTime = new Timestamp(System.currentTimeMillis())
    } yield MarketDataPerDay(tradeDate, symbol, market, basePrice, volume, adjFactor, adjValuePrice, lastValuePrice,
      adjVolume, vwap, tradeAmount, listingVolume, avgVolume, tickCount, avgQuoteVolume, mainMarket, updateTime)
  }

}

object MarketDataPerDayUploader extends LazyLogging {

  final val CONFIG_BASE = "MarketDataPerDayUploader"
  private val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {

    try {
      ServiceInjector.initialize()
      val marketDataPerDayUploader = ServiceInjector.getComponent(classOf[MarketDataPerDayUploader])

      val uploadInfoSeq = Uploaders.uploadInformationSeq(config.getConfig(CONFIG_BASE))

      uploadInfoSeq.foreach {i =>
        logger.info(s"$i execution started.")
        marketDataPerDayUploader.upload(i)
        logger.info(s"$i execution ended.")
      }
    } catch {
      case e: Exception => logger.error("Error occurred.", e)
    }

  }

}
