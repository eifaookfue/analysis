package jp.co.nri.tcatool.sba.tca.model

import java.sql.Timestamp

import jp.co.nri.tcatool.sba.model.{EHistoryType, Mapper}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class HistoryKey(
                     orderKey: OrderKey,
                     historyNo: Int
                     )

case class OrderHistory(
                       historyKey: HistoryKey,
                       historyType: EHistoryType,
                       price: Option[BigDecimal],
                       sliceQty: Int,
                       cumulatives: Cumulatives,
                       avgPrice: Option[BigDecimal],
                       sliceTime: java.sql.Timestamp,
                       note: Note,
                       strategyName: String,
                       ackedTime: Option[Timestamp],
                       closedTime: Option[Timestamp],
                       updateTime: Timestamp
                       )

trait OrderHistoryComponent extends Mapper {

  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class OrderHistories(tag: Tag) extends Table[OrderHistory](tag, "TCA_ORDER_HISTORY") {
    def compId = column[String]("COMP_ID", O.Length(4))
    def orderId = column[String]("ORDERID", O.Length(11))
    def orderHistoryNo = column[Int]("ORDER_HISTORY_NO")
    def historyType = column[EHistoryType]("HISTORY_TYPE", O.Length(10))
    def price = column[Option[BigDecimal]]("PRICE")
    def sliceQty = column[Int]("SLICE_QTY")
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
    def sliceTime = column[Timestamp]("SLICE_TIME")
    def note = column[Option[String]]("NOTE", O.Length(500))
    def inHouseNote = column[Option[String]]("IN_HOUSE_NOTE", O.Length(500))
    def strategyName = column[String]("STRATEGY_NAME", O.Length(20))
    def ackedTime = column[Option[Timestamp]]("ACKED_TIME")
    def closedTime = column[Option[Timestamp]]("CLOSED_TIME")
    def updateTime = column[Timestamp]("UPDATE_TIME" ,SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def * = (historyKeyProjection, historyType, price, sliceQty,
      cumulativesProjection, avgPrice, sliceTime, noteProjection,
      strategyName, ackedTime, closedTime, updateTime) <> (OrderHistory.tupled, OrderHistory.unapply)
    def historyKeyProjection = (orderKeyProjection, orderHistoryNo) <> (HistoryKey.tupled, HistoryKey.unapply)
    def orderKeyProjection = (compId, orderId) <> (OrderKey.tupled, OrderKey.unapply)
    def noteProjection = (inHouseNote, note) <> (Note.tupled, Note.unapply)
    def pk = primaryKey("ORDER_HISTORY_PK", (compId, orderId, orderHistoryNo))
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
