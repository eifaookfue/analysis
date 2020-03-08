package jp.co.nri.nefs.tool.analytics.model.client

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait WindowSliceComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class WindowSlices(tag: Tag) extends Table[(String, String, String, Int, Int, Double)](tag, "WINDOW_SLICE") {
    def slice = column[String]("SLICE", O.Length(9))
    def userId = column[String]("USER_ID", O.Length(20))
    def windowName = column[String]("WINDOW_NAME", O.Length(48))
    def count = column[Int]("COUNT")
    def startupCount = column[Int]("STARTUP_COUNT")
    def avgStartup = column[Double]("AVG_STARTUP")
    //def * = (slice, userId, windowName, count, avgStartup) <> (WindowSlice.tupled, WindowSlice.unapply)
    //def * = (slice, userId, windowName, count) <> (WindowSlice.tupled, WindowSlice.unapply)
    //def * = (slice, userId, count) <> (WindowSlice.tupled, WindowSlice.unapply)
    //def * = (slice, userId, windowName, count, startupCount, avgStartup)
    def * = (slice, userId, windowName, count, startupCount, avgStartup)
    //def pk = primaryKey("pk_1", (slice, userId, windowName))
    def pk = primaryKey("pk_1", (slice, userId, windowName))
  }
}
