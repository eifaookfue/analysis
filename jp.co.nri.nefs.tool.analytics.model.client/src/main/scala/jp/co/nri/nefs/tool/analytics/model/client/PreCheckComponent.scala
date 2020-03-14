package jp.co.nri.nefs.tool.analytics.model.client

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class PreCheck(logId: Int, lineNo: Long, windowName: Option[String], code: String, message: String)

trait PreCheckComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class PreChecks(tag: Tag) extends Table[PreCheck](tag, "PRE_CHECK") {
    def logId = column[Int]("LOG_ID")
    def lineNo = column[Long]("LINE_NO")
    def windowName = column[Option[String]]("WINDOW_NAME")
    def code = column[String]("CODE", O.Length(10))
    def message = column[String]("MESSAGE", O.Length(200))
    def * = (logId, lineNo, windowName, code, message) <> (PreCheck.tupled, PreCheck.unapply)
    def pk = primaryKey("pk_1", (logId, lineNo))
  }
}
