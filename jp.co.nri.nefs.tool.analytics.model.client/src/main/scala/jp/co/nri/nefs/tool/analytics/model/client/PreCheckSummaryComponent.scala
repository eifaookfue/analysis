package jp.co.nri.nefs.tool.analytics.model.client

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class PreCheckSummary(message: String, windowName: String, count: Int)

trait PreCheckSummaryComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class PreCheckSummaries(tag: Tag) extends Table[PreCheckSummary](tag, "PRE_CHECK_SUMMARY") {
    def message = column[String]("MESSAGE")
    def windowName = column[String]("WINDOW_NAME")
    def count = column[Int]("COUNT")
    def * = (message, windowName, count) <> (PreCheckSummary.tupled, PreCheckSummary.unapply)
    def pk = primaryKey("PRE_CHECK_SUMMARY_PK_1", (message, windowName))
  }
}
