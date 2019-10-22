package dao

import jp.co.nri.nefs.tool.log.common.model.Log
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait LogComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class Logs(tag: Tag) extends Table[Log](tag, "LOG") {
    def logId: Rep[Long] = column[Long]("LOG_ID", O.PrimaryKey, O.AutoInc)
    def appName: Rep[String] = column[String]("APP_NAME")
    def computerName: Rep[String] = column[String]("COMPUTER_NAME")
    def userId: Rep[String] = column[String]("USER_ID")
    def tradeDate: Rep[String] = column[String]("TRADE_DATE")
    def * = (logId, appName, computerName, userId, tradeDate) <> (Log.tupled, Log.unapply)
  }
}

