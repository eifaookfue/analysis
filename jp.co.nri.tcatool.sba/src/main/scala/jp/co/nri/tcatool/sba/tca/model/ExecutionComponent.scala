package jp.co.nri.tcatool.sba.tca.model

import java.sql.Timestamp

import jp.co.nri.tcatool.ref.reference.model.{EMarket, Mapper}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class Execution(
                      executionId: String,
                      historyKey: HistoryKey,
                      price: BigDecimal,
                      execQty: Int,
                      executionTime: java.sql.Timestamp,
                      market: EMarket,
                      note: Option[String],
                      updateTime: Timestamp
                    )

trait ExecutionComponent extends Mapper {

  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class Executions(tag: Tag) extends Table[Execution](tag, "TCA_EXECUTION") {
    def executionId = column[String]("EXECUTION_ID", O.Length(16), O.PrimaryKey)
    def compId = column[String]("COMP_ID", O.Length(4))
    def orderId = column[String]("ORDERID", O.Length(11))
    def orderHistoryNo = column[Int]("ORDER_HISTORY_NO")
    def price = column[BigDecimal]("PRICE")
    def execQty = column[Int]("EXEC_QTY")
    def executionTime = column[Timestamp]("EXECUTION_TIME")
    def market = column[EMarket]("MARKET", O.Length(10))
    def note = column[Option[String]]("NOTE", O.Length(500))
    def updateTime = column[Timestamp]("UPDATE_TIME" ,SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def * = (executionId, historyKeyProjection, price, execQty, executionTime,
      market, note, updateTime) <> (Execution.tupled, Execution.unapply)
    def orderKeyProjection = (compId, orderId) <> (OrderKey.tupled, OrderKey.unapply)
    def historyKeyProjection = (orderKeyProjection, orderHistoryNo) <>
      (HistoryKey.tupled, HistoryKey.unapply)
    def idx = index("TCA_EXECUTION_IDX_1", (compId, orderId), unique = false)
  }

}
