package dao

import java.sql.Timestamp
import javax.inject.{ Inject, Singleton }
import models.Window
import scala.concurrent.ExecutionContext
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

trait WindowDetailComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class WindowDetail(tag: Tag) extends Table[Window](tag, "WINDOW_DETAIL") {
    def handler = column[String]("HANDLER")

    def windowName = column[Option[String]]("WINDOW_NAME")

    def action = column[Option[String]]("ACTION")

    def method = column[Option[String]]("METHOD")

    def userName = column[String]("USER_NAME")

    def tradeDate = column[String]("TRADE_DATE")

    def time = column[Timestamp]("TIME")

    def startupTime = column[Long]("STARTUP_TIME")

    def * = (handler, windowName, action, method, userName, tradeDate, time, startupTime) <> (Window.tupled, Window.unapply)
  }

}
@Singleton()
class WindowDetailDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends WindowDetailComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val details = TableQuery[WindowDetail]

}
