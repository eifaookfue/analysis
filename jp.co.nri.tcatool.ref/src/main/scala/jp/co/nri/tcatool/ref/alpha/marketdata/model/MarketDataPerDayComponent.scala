package jp.co.nri.tcatool.ref.alpha.marketdata.model

import java.sql.Timestamp
import java.time.LocalDate

import jp.co.nri.tcatool.ref.alpha.common.model.AvgQuoteVolume
import jp.co.nri.tcatool.ref.reference.model.{EMarket, Mapper}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class BasePrice(
               open: Option[BigDecimal],
               high: Option[BigDecimal],
               low: Option[BigDecimal],
               close: Option[BigDecimal]
               )

case class AvgVolume(
                    per5: Option[BigDecimal],
                    per10: Option[BigDecimal]
                    )

case class VWAP(
                 allDay: Option[BigDecimal],
                 am: Option[BigDecimal],
                 pm: Option[BigDecimal]
               )

case class MarketDataPerDay(
                           tradeDate: LocalDate,
                           symbol: String,
                           market: Option[EMarket],
                           price: BasePrice,
                           volume: Option[Int],
                           adjFactor: Option[BigDecimal],
                           adjValuePrice: Option[BigDecimal],
                           lastValuePrice: Option[BigDecimal],
                           adjVolume: Option[BigDecimal],
                           vwap: VWAP,
                           tradeAmount: Option[BigDecimal],
                           listingVolume: Option[BigDecimal],
                           avgVolume: AvgVolume,
                           tickCount: Option[Int],
                           avgQuoteVolume: AvgQuoteVolume,
                           mainMarket: Option[EMarket],
                           updateTime: Timestamp
                           )

trait MarketDataPerDayComponent extends Mapper {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class MarketDataPerDays(tag: Tag) extends Table[MarketDataPerDay](tag, "ALPHA_MARKET_DATA_PER_DAY") {
    def tradeDate = column[LocalDate]("TRADE_DATE")
    def symbol = column[String]("SYMBOL", O.Length(10))
    def market = column[Option[EMarket]]("MARKET", O.Length(10))
    def openPrice = column[Option[BigDecimal]]("OPEN_PRICE")
    def highPrice = column[Option[BigDecimal]]("HIGH_PRICE")
    def lowPrice = column[Option[BigDecimal]]("LOW_PRICE")
    def closePrice = column[Option[BigDecimal]]("CLOSE_PRICE")
    def volume = column[Option[Int]]("VOLUME")
    def adjFactor = column[Option[BigDecimal]]("ADF_FACTOR")
    def adjValuePrice = column[Option[BigDecimal]]("ADJ_VALUE_PRICE")
    def lastValuePrice = column[Option[BigDecimal]]("LAST_VALUE_PRICE")
    def adjVolume = column[Option[BigDecimal]]("ADJ_VOLUME")
    def dayVwap = column[Option[BigDecimal]]("DAY_VWAP")
    def amVwap = column[Option[BigDecimal]]("AM_VWAP")
    def pmVwap = column[Option[BigDecimal]]("PM_VWAP")
    def tradeAmount = column[Option[BigDecimal]]("TRADE_AMOUNT")
    def listingVolume = column[Option[BigDecimal]]("LISTING_VOLUME")
    def avgVolume5 = column[Option[BigDecimal]]("AVG_VOLUME_5")
    def avgVolume10 = column[Option[BigDecimal]]("AVG_VOLUME_10")
    def tickCount = column[Option[Int]]("TICK_COUNT")
    def avgAskVolume = column[Option[BigDecimal]]("AVG_ASK_VOLUME")
    def avgBidVolume = column[Option[BigDecimal]]("AVG_BID_VOLUME")
    def mainMarket = column[Option[EMarket]]("MAIN_MARKET", O.Length(10))
    def updateTime = column[Timestamp]("UPDATE_TIME")
    def pk = primaryKey("ALPHA_MARKET_DATA_PER_DAY_PK_1", (tradeDate, symbol))

    def * = (tradeDate, symbol, market, priceProjection, volume,
      adjFactor, adjValuePrice, lastValuePrice, adjVolume,
      vwapProjection, tradeAmount, listingVolume,
      avgVolumeProjection, tickCount, avgQuoteVolumeProjection, mainMarket, updateTime) <>
      (MarketDataPerDay.tupled, MarketDataPerDay.unapply)

    def priceProjection = (openPrice, highPrice, lowPrice, closePrice) <>
      (BasePrice.tupled, BasePrice.unapply)
    def avgQuoteVolumeProjection = (avgAskVolume, avgBidVolume) <> (AvgQuoteVolume.tupled, AvgQuoteVolume.unapply)
    def avgVolumeProjection = (avgVolume5, avgVolume10) <> (AvgVolume.tupled, AvgVolume.unapply)
    def vwapProjection = (dayVwap, amVwap, pmVwap) <> (VWAP.tupled, VWAP.unapply)
  }

}
