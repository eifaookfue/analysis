package jp.co.nri.nefs.tool.analytics.model.client

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class WindowUser(userId: String, windowName: String, count: Int)

trait WindowUserComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class WindowUsers(tag: Tag) extends Table[WindowUser](tag, "WINDOW_USER") {
    def userId = column[String]("USER_ID", O.Length(10))
    def windowName = column[String]("WINDOW_NAME", O.Length(48))
    def count = column[Int]("COUNT")
    def * = (userId, windowName, count) <> (WindowUser.tupled, WindowUser.unapply)
    def pk = primaryKey("WINDOW_USER_PK_1", (userId, windowName))
  }
}
