package jp.co.nri.nefs.tool.analytics.model.client

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class WindowDetail(logId: Int, lineNo: Int,
                        activator: Option[String], windowName: Option[String], destinationType: Option[String],
                        action: Option[String], method: Option[String],
                        time: Timestamp, startupTime: Option[Long], updateTime: Timestamp = null)

trait WindowDetailComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class WindowDetails(tag: Tag) extends Table[WindowDetail](tag, "WINDOW_DETAIL") {
    def logId = column[Int]("LOG_ID")
    def lineNo = column[Int]("LINE_NO", O.Length(20))
    def activator = column[Option[String]]("ACTIVATOR")
    def windowName = column[Option[String]]("WINDOW_NAME")
    def destinationType = column[Option[String]]("DESTINATION_TYPE")
    def action = column[Option[String]]("ACTION")
    def method = column[Option[String]]("METHOD")
    def time = column[Timestamp]("TIME")
    def startupTime = column[Option[Long]]("STARTUP_TIME")
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def * = (logId, lineNo, activator, windowName, destinationType, action, method, time, startupTime, updateTime) <> (WindowDetail.tupled, WindowDetail.unapply)
    def pk = primaryKey("WINDOW_DETAIL_PK_1", (logId, lineNo))
  }

}
