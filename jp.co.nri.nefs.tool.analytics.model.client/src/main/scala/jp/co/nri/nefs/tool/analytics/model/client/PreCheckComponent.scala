package jp.co.nri.nefs.tool.analytics.model.client

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class PreCheck(logId: Int, lineNo: Int, windowName: Option[String], code: String, message: String)

case class PreCheckEx(preCheck: PreCheck, updateTime: Timestamp)

trait PreCheckComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class PreChecks(tag: Tag) extends Table[PreCheckEx](tag, "PRE_CHECK") {
    def logId = column[Int]("LOG_ID")
    def lineNo = column[Int]("LINE_NO")
    def windowName = column[Option[String]]("WINDOW_NAME")
    def code = column[String]("CODE", O.Length(10))
    def message = column[String]("MESSAGE", O.Length(500))
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def preCheckProjection = (logId, lineNo, windowName, code, message) <> (PreCheck.tupled, PreCheck.unapply)
    def * = (preCheckProjection, updateTime) <> (PreCheckEx.tupled, PreCheckEx.unapply)
    def pk = primaryKey("PRE_CHECK_PK_1", (logId, lineNo))
  }
}
