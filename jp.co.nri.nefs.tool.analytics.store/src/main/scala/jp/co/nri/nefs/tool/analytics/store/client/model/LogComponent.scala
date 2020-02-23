package jp.co.nri.nefs.tool.analytics.store.client.model

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait LogComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class Logs(tag: Tag) extends Table[Log](tag, "LOG") {
    def logId: Rep[Long] = column[Long]("LOG_ID", O.PrimaryKey, O.AutoInc)
    def appName: Rep[String] = column[String]("APP_NAME", O.Length(20))
    def computerName: Rep[String] = column[String]("COMPUTER_NAME", O.Length(20))
    def userId: Rep[String] = column[String]("USER_ID", O.Length(20))
    def tradeDate: Rep[String] = column[String]("TRADE_DATE", O.Length(8))
    def time: Rep[Timestamp] = column[Timestamp]("TIME")
    def fileName: Rep[String] = column[String]("FILE_NAME", O.Length(60))
    def * = (logId, appName, computerName, userId, tradeDate, time, fileName) <> (Log.tupled, Log.unapply)
    def idx_1 = index("idx_1", fileName, unique = true)
  }
}
