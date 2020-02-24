package jp.co.nri.nefs.tool.analytics.model.common

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait UserComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "USER") {
    def userId: Rep[String] = column[String]("USER_ID", O.Length(10), O.PrimaryKey)
    def userName: Rep[String] = column[String]("USER_NAME", O.Length(20))
    def * = (userId, userName) <> (User.tupled, User.unapply)
  }

}
