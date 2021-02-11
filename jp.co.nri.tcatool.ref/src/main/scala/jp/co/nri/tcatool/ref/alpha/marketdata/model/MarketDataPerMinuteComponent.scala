package jp.co.nri.tcatool.ref.alpha.marketdata.model

import java.sql.Timestamp
import java.time.LocalDate

import jp.co.nri.tcatool.ref.reference.model.{EMarket, ESessionType, Mapper}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class Price(time: Option[Timestamp], price: Option[BigDecimal])
case class ExcessVolume(normal: BigDecimal, ask: BigDecimal, bid: BigDecimal)

case class MarketDataPerMinute(
                              tradeDate: LocalDate,
                              symbol: String,
                              market: EMarket,
                              tradeTime: Timestamp,
                              tradePrice: Option[BigDecimal],
                              tradeVolume: Int,
                              sessionCode: ESessionType,
                              currentPrice: Option[BigDecimal],
                              tradeAmount: BigDecimal,
                              tradeCount: Int,
                              sellQuote: Option[BigDecimal],
                              buyQuote: Option[BigDecimal],
                              cumVolume: Int,
                              cumAmount: BigDecimal,
                              spread: Option[BigDecimal],
                              askVolume: Option[BigDecimal],
                              bidVolume: Option[BigDecimal],
                              highPrice: Price,
                              lowPrice: Price,
                              excessVolume: ExcessVolume,
                              updateTime: Timestamp
                              )

trait MarketDataPerMinuteComponent extends Mapper {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class MarketDataPerMinutes(tag: Tag) extends Table[MarketDataPerMinute](tag, "ALPHA_MARKET_DATA_PER_MINUTE") {
    def tradeDate = column[LocalDate]("TRADE_DATE")
    def symbol  = column[String]("SYMBOL")
    def market  = column[EMarket]("MARKET")
    def tradeTime = column[Timestamp]("TRADE_TIME")
    def currentPrice  = column[Option[BigDecimal]]("CURRENT_PRICE")
    def tradePrice  = column[Option[BigDecimal]]("TRADE_PRICE")
    def tradeVolume  = column[Int]("TRADE_VOLUME")
    def sessionCode = column[ESessionType]("SESSION_CODE")
    def excessVolume  = column[BigDecimal]("EXCESS_VOLUME")
    def tradeAmount  = column[BigDecimal]("TRADE_AMOUNT")
    def tradeCount  = column[Int]("TRADE_COUNT")
    def sellQuote  = column[Option[BigDecimal]]("SELL_QUOTE")
    def buyQuote  = column[Option[BigDecimal]]("BUY_QUOTE")
    def cumVolume  = column[Int]("CUM_VOLUME")
    def cumAmount  = column[BigDecimal]("CUM_AMOUNT")
    def spread  = column[Option[BigDecimal]]("SPREAD", O.SqlType("NUMBER(12,7)"))
    def askVolume  = column[Option[BigDecimal]]("ASK_VOLUME")
    def bidVolume  = column[Option[BigDecimal]]("BID_VOLUME")
    def highPrice  = column[Option[BigDecimal]]("HIGH_PRICE")
    def lowPrice  = column[Option[BigDecimal]]("LOW_PRICE")
    def highPriceTime  = column[Option[Timestamp]]("HIGH_PRICE_TIME")
    def lowPriceTime  = column[Option[Timestamp]]("LOW_PRICE_TIME")
    def askExcessVolume = column[BigDecimal]("ASK_EXCESS_VOLUME")
    def bidExcessVolume = column[BigDecimal]("BID_EXCESS_VOLUME")
    def updateTime  = column[Timestamp]("UPDATE_TIME")
    def * = (tradeDate, symbol, market, tradeTime, tradePrice, tradeVolume, sessionCode, currentPrice, tradeAmount,
    tradeCount, sellQuote, buyQuote, cumVolume, cumAmount, spread, askVolume, bidVolume, highProjection, lowProjection, excessProjection, updateTime) <>
      (MarketDataPerMinute.tupled, MarketDataPerMinute.unapply)
    def pk = primaryKey("ALPHA_MARKET_DATA_PER_MINUTE_PK_1", (tradeDate, symbol, market, tradeTime, sessionCode))

    def highProjection = (highPriceTime, highPrice) <> (Price.tupled, Price.unapply)
    def lowProjection = (lowPriceTime, lowPrice) <> (Price.tupled, Price.unapply)
    def excessProjection = (excessVolume, askExcessVolume, bidExcessVolume) <> (ExcessVolume.tupled, ExcessVolume.unapply)

  }

}
