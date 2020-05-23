package jp.co.nri.nefs.tool.analytics.model.client

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class WindowDate(date: String, windowName: String, count: Int)

trait WindowDateComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class WindowDates(tag: Tag) extends Table[WindowDate](tag, "WINDOW_DATE") {
    def tradeDate = column[String]("TRADE_DATE", O.Length(8))
    def windowName = column[String]("WINDOW_NAME", O.Length(48))
    def count = column[Int]("COUNT")
    def * = (tradeDate, windowName, count) <> (WindowDate.tupled, WindowDate.unapply)
    def pk = primaryKey("WINDOW_DATE_PK_1", (tradeDate, windowName))
  }

}
