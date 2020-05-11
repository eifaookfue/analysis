package jp.co.nri.nefs.tool.analytics.model.client

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class PreCheck(logId: Int, lineNo: Long, windowName: Option[String], code: String, message: String, updateTime: Timestamp = null)

trait PreCheckComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class PreChecks(tag: Tag) extends Table[PreCheck](tag, "PRE_CHECK") {
    def logId = column[Int]("LOG_ID")
    def lineNo = column[Long]("LINE_NO")
    def windowName = column[Option[String]]("WINDOW_NAME")
    def code = column[String]("CODE", O.Length(10))
    def message = column[String]("MESSAGE", O.Length(200))
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def * = (logId, lineNo, windowName, code, message, updateTime) <> (PreCheck.tupled, PreCheck.unapply)
    def pk = primaryKey("PRE_CHECK_PK_1", (logId, lineNo))
  }
}
