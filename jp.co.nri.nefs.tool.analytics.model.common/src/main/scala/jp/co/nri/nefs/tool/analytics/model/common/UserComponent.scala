package jp.co.nri.nefs.tool.analytics.model.common

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

trait UserComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "USER") {
    def userId: Rep[String] = column[String]("USER_ID", O.Length(10), O.PrimaryKey)
    def userName: Rep[String] = column[String]("USER_NAME", O.Length(20))
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))
    def * = (userId, userName, updateTime) <> (User.tupled, User.unapply)
  }

}
