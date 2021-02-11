package jp.co.nri.tcatool.sba.tca.model

import java.sql.Timestamp
import java.time.LocalDate

import jp.co.nri.tcatool.ref.reference.model.{EBSType, EMarket, ESessionType, Mapper => RefMapper}
import jp.co.nri.tcatool.sba.model.{Mapper => SBAMapper}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class CumulativeValue(
                         quantity: Int,
                         cash: BigDecimal
                         )

case class Cumulatives(
                       total: CumulativeValue,
                       tse: CumulativeValue,
                       chj: CumulativeValue,
                       jnx: CumulativeValue,
                       pts: CumulativeValue,
                       dkpl: CumulativeValue
                     )

case class Order(
                  base: OrderBase,
                  cumulatives: Cumulatives,
                  basePrice: Option[BigDecimal],
                  orderAmount: Option[BigDecimal],
                  volume: Option[Int],
                  avgPrice: Option[BigDecimal],
                  benchMark: BenchMark,
                  closedTime: Option[Timestamp],
                  changeRatio: Option[BigDecimal],
                  spread: Option[BigDecimal],
                  updateTime: Timestamp
                )

case class OrderKey(
                     compId: String,
                     orderId: String,
                   )

case class OrderBase(
                      orderKey: OrderKey,
                      baseDate: LocalDate,
                      sessionType: ESessionType,
                      symbol: String,
                      lot: Int,
                      bsType: EBSType,
                      orderQty: Int,
                      price: Option[BigDecimal],
                      sliceQty: Int,
                      market: Option[EMarket],
                      brokerId: String,
                      sliceDate: LocalDate,
                      sliceTime: java.sql.Timestamp,
                      note: Note,
                      strategyName: String
                    )
case class Slippage(
                   baseValue: Option[BigDecimal],
                   evaluatedValue: Option[BigDecimal]
                   )
case class BenchMark(
                    VWAP: Slippage,
                    periodVWAP: Slippage,
                    periodTWAP: Slippage,
                    arrivalPrice: Slippage
                    )
case class Note(external: Option[String], internal: Option[String])

trait OrderComponent extends SBAMapper with RefMapper {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class Orders(tag: Tag) extends Table[Order](tag, "TCA_ORDER") {
    def compId = column[String]("COMP_ID", O.Length(4))
    def orderId = column[String]("ORDERID", O.Length(11))
    def baseDate = column[LocalDate]("BASE_DATE", O.Length(8))
    def sessionType = column[ESessionType]("SESSION_TYPE", O.Length(10))
    def symbol = column[String]("SYMBOL", O.Length(10))
    def lot = column[Int]("LOT")
    def bsType = column[EBSType]("BS_TYPE", O.Length(10))
    def orderQty = column[Int]("ORDERQTY")
    def price = column[Option[BigDecimal]]("PRICE")
    def sliceQty = column[Int]("SLICEQTY")
    def cumQtyTotal = column[Int]("CUM_QTY_TOTAL")
    def cumCashTotal = column[BigDecimal]("CUM_CASH_TOTAL")
    def cumQtyTSE = column[Int]("CUM_QTY_TSE")
    def cumCashTSE = column[BigDecimal]("CUM_CASH_TSE")
    def cumQtyCHJ = column[Int]("CUM_QTY_CHJ")
    def cumCashCHJ = column[BigDecimal]("CUM_CASH_CHJ")
    def cumQtyJNX = column[Int]("CUM_QTY_JNX")
    def cumCashJNX = column[BigDecimal]("CUM_CASH_JNX")
    def cumQtyPTS = column[Int]("CUM_QTY_PTS")
    def cumCashPTS = column[BigDecimal]("CUM_CASH_PTS")
    def cumQtyDKPL = column[Int]("CUM_QTY_DKPL")
    def cumCashDKPL = column[BigDecimal]("CUM_CASH_DKPL")
    def avgPrice = column[Option[BigDecimal]]("AVG_PRICE", O.SqlType("NUMBER(12,4)"))
    def VWAP = column[Option[BigDecimal]]("VWAP", O.SqlType("NUMBER(12,4)"))
    def slippageVWAP = column[Option[BigDecimal]]("SLIPPAGE_VWAP", O.SqlType("NUMBER(12,7)"))
    def periodVWAP = column[Option[BigDecimal]]("PERIOD_VWAP", O.SqlType("NUMBER(12,4)"))
    def slippagePeriodVWAP = column[Option[BigDecimal]]("SLIPPAGE_PERIOD_VWAP", O.SqlType("NUMBER(12,7)"))
    def periodTWAP = column[Option[BigDecimal]]("PERIOD_TWAP", O.SqlType("NUMBER(12,4)"))
    def slippagePeriodTWAP = column[Option[BigDecimal]]("SLIPPAGE_PERIOD_TWAP", O.SqlType("NUMBER(12,7)"))
    def arrivalPrice = column[Option[BigDecimal]]("ARRIVAL_PRICE", O.SqlType("NUMBER(12,4)"))
    def slippageArrivalPrice = column[Option[BigDecimal]]("SLIPPAGE_ARRIVAL_PRICE", O.SqlType("NUMBER(12,4)"))
    def basePrice = column[Option[BigDecimal]]("BASE_PRICE", O.SqlType("NUMBER(12,4)"))
    def orderAmount = column[Option[BigDecimal]]("ORDER_AMOUNT")
    def volume = column[Option[Int]]("VOLUME")
    def market = column[Option[EMarket]]("MARKET", O.Length(10))
    def brokerId = column[String]("BROKER_ID", O.Length(3))
    def sliceDate = column[LocalDate]("SLICE_DATE")
    def sliceTime = column[Timestamp]("SLICE_TIME")
    def note = column[Option[String]]("NOTE", O.Length(500))
    def inHouseNote = column[Option[String]]("IN_HOUSE_NOTE", O.Length(500))
    def strategyName = column[String]("STRATEGY_NAME", O.Length(20))
    def closedTime = column[Option[Timestamp]]("CLOSED_TIME")
    def changeRatio = column[Option[BigDecimal]]("CHANGE_RATIO", O.SqlType("NUMBER(12,7)"))
    def spread = column[Option[BigDecimal]]("SPREAD", O.SqlType("NUMBER(12,7)"))
    def updateTime = column[Timestamp]("UPDATE_TIME" ,SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def * = (orderBaseProjection, cumulativesProjection, basePrice, orderAmount, volume, avgPrice,
      benchMarkProjection, closedTime, changeRatio, spread, updateTime) <> (Order.tupled, Order.unapply)
    def pk = primaryKey("TCA_ORDER_PK", (compId, orderId))
    def orderBaseProjection = (orderKeyProjection, baseDate, sessionType, symbol,
      lot, bsType, orderQty,price, sliceQty, market, brokerId, sliceDate, sliceTime, noteProjection,
      strategyName) <> (OrderBase.tupled, OrderBase.unapply)
    def orderKeyProjection = (compId, orderId) <> (OrderKey.tupled, OrderKey.unapply)
    def VWAPProjection = (VWAP, slippageVWAP) <> (Slippage.tupled, Slippage.unapply)
    def periodVWAPProjection = (periodVWAP, slippagePeriodVWAP) <> (Slippage.tupled, Slippage.unapply)
    def periodTWAPProjection = (periodTWAP, slippagePeriodTWAP) <> (Slippage.tupled, Slippage.unapply)
    def arrivalPriceProjection = (arrivalPrice, slippageArrivalPrice) <>
      (Slippage.tupled, Slippage.unapply)
    def benchMarkProjection = (VWAPProjection, periodVWAPProjection,
      periodTWAPProjection, arrivalPriceProjection) <> (BenchMark.tupled, BenchMark.unapply)
    def noteProjection = (inHouseNote, note) <> (Note.tupled, Note.unapply)
    def cumulativeValueTotalProjection =
      (cumQtyTotal, cumCashTotal) <> (CumulativeValue.tupled, CumulativeValue.unapply)
    def cumulativeValueTSEProjection =
      (cumQtyTSE, cumCashTSE) <> (CumulativeValue.tupled, CumulativeValue.unapply)
    def cumulativeValueCHJProjection =
      (cumQtyCHJ, cumCashCHJ) <> (CumulativeValue.tupled, CumulativeValue.unapply)
    def cumulativeValueJNXProjection =
      (cumQtyJNX, cumCashJNX) <> (CumulativeValue.tupled, CumulativeValue.unapply)
    def cumulativeValuePTSProjection =
      (cumQtyPTS, cumCashPTS) <> (CumulativeValue.tupled, CumulativeValue.unapply)
    def cumulativeValueDKPLProjection =
      (cumQtyDKPL, cumCashDKPL) <> (CumulativeValue.tupled, CumulativeValue.unapply)
    def cumulativesProjection =
      (cumulativeValueTotalProjection, cumulativeValueTSEProjection, cumulativeValueCHJProjection,
        cumulativeValueJNXProjection, cumulativeValuePTSProjection,
        cumulativeValueDKPLProjection) <> (Cumulatives.tupled, Cumulatives.unapply)
  }
}
