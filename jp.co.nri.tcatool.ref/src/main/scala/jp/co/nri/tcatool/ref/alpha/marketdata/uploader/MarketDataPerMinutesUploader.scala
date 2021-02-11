package jp.co.nri.tcatool.ref.alpha.marketdata.uploader

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime, LocalTime}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.common.upload.{DBUtil, SimpleUploader, Uploaders}
import jp.co.nri.tcatool.ref.alpha.marketdata.model.{ExcessVolume, MarketDataPerMinute, MarketDataPerMinuteComponent, Price}
import jp.co.nri.tcatool.ref.reference.model.{EMarket, ESessionType}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.util.Try


@Singleton()
class MarketDataPerMinutesUploader @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends MarketDataPerMinuteComponent with SimpleUploader[MarketDataPerMinute] with DBUtil[MarketDataPerMinute]
    with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging {

  import profile.api._

  override val query = TableQuery[MarketDataPerMinutes]

  override def convert(row: List[String]): Try[MarketDataPerMinute] = {

    import jp.co.nri.tcatool.ref.alpha.common.read.AlphaRead._

    val tradeDateTry = Read[LocalDate].reads(row.head)
    val tradeTry = createPrice(tradeDateTry, row(3), row(6))
    val highTry = createPrice(tradeDateTry, row(18), row(20))
    val lowTry = createPrice(tradeDateTry, row(19), row(21))
    for {
      tradeDate <- tradeDateTry
      symbol <- Read[String].reads(row(1))
      market <- Read[EMarket].reads(row(2))
      tradeTime <- tradeTry.map(_.time.get)
      tradePrice <- tradeTry.map(_.price)
      tradeVolume <- Read[BigDecimal].reads(row(7)).map(_.toInt)
      sessionCode <- Read[ESessionType].reads(row(4))
      currentPrice <- Read[BigDecimal].optionalReads(row(5))
      tradeAmount <- Read[BigDecimal].reads(row(9))
      tradeCount <- Read[Int].reads(row(10))
      sellQuote <- Read[BigDecimal].optionalReads(row(16))
      buyQuote <- Read[BigDecimal].optionalReads(row(17))
      cumVolume <- Read[BigDecimal].reads(row(13)).map(_.toInt)
      cumAmount <- Read[BigDecimal].reads(row(14))
      spread <- Read[BigDecimal].optionalReads(row(15))
      askVolume <- Read[BigDecimal].optionalReads(row(16))
      bidVolume <- Read[BigDecimal].optionalReads(row(17))
      high <- highTry
      low <- lowTry
      normalExcessVolume <- Read[BigDecimal].reads(row(8))
      askExcessVolume <- Read[BigDecimal].reads(row(22))
      bidExcessVolume <- Read[BigDecimal].reads(row(23))
      excessVolume = ExcessVolume(normalExcessVolume, askExcessVolume, bidExcessVolume)
      updateTime = new Timestamp(System.currentTimeMillis())
    } yield MarketDataPerMinute(tradeDate, symbol, market, tradeTime, tradePrice, tradeVolume, sessionCode, currentPrice, tradeAmount, tradeCount, sellQuote, buyQuote, cumVolume,
      cumAmount, spread, askVolume, bidVolume, high, low, excessVolume, updateTime)

  }

  private def createPrice(tradeDateTry: Try[LocalDate], timeStr: String, priceStr: String): Try[Price] = {
    import jp.co.nri.tcatool.common.read.Read.timeMinutesRead
    for {
      tradeDate <- tradeDateTry
      time <- Read[LocalTime](timeMinutesRead).optionalReads(timeStr)
      dateTime = time.map(LocalDateTime.of(tradeDate, _))
      timestamp = dateTime.map(Timestamp.valueOf)
      price <- Read[BigDecimal].optionalReads(priceStr)
    } yield Price(timestamp, price)
  }


}

object MarketDataPerMinutesUploader extends LazyLogging {

  final val CONFIG_BASE = "MarketDataPerMinutesUploader"

  private val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {

    try {
      ServiceInjector.initialize()
      val marketDataPerMinutesUploader = ServiceInjector.getComponent(classOf[MarketDataPerMinutesUploader])

      val uploadInfoSeq = Uploaders.uploadInformationSeq(config.getConfig(CONFIG_BASE))

      uploadInfoSeq.foreach {i =>
        logger.info(s"$i execution started.")
        marketDataPerMinutesUploader.upload(i)
        logger.info(s"$i execution ended.")
      }
    } catch {
      case e: Exception => logger.error("Error occurred.", e)
    }

  }

}
