package jp.co.nri.nefs.tool.analytics.model.client

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class Log(logId: Int, appName: String, computerName: String, userId:String,
                tradeDate: String, time: Timestamp, fileName: String)

case class LogEx(log: Log, updateTime: Timestamp)

trait LogComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class Logs(tag: Tag) extends Table[LogEx](tag, "LOG") {
    def logId = column[Int]("LOG_ID", O.PrimaryKey, O.AutoInc)
    def appName = column[String]("APP_NAME", O.Length(20))
    def computerName = column[String]("COMPUTER_NAME", O.Length(20))
    def userId = column[String]("USER_ID", O.Length(20))
    def tradeDate = column[String]("TRADE_DATE", O.Length(8))
    def time = column[Timestamp]("TIME")
    def fileName = column[String]("FILE_NAME", O.Length(100))
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def logProjection = (logId, appName, computerName, userId, tradeDate, time, fileName) <> (Log.tupled, Log.unapply)
    def * = (logProjection, updateTime) <> (LogEx.tupled, LogEx.unapply)
    def uk_1 = index("LOG_UK_1", fileName, unique = true)
  }
}
