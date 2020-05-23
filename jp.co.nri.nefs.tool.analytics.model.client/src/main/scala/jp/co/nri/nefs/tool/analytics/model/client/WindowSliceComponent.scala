package jp.co.nri.nefs.tool.analytics.model.client

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class WindowSlice(slice: String, windowName: String, count: Int)

trait WindowSliceComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class WindowSlices(tag: Tag) extends Table[WindowSlice](tag, "WINDOW_SLICE") {
    def slice = column[String]("SLICE", O.Length(9))
    def windowName = column[String]("WINDOW_NAME", O.Length(48))
    def count = column[Int]("COUNT")
    def * = (slice, windowName, count) <> (WindowSlice.tupled, WindowSlice.unapply)
    def pk = primaryKey("WINDOW_SLICE_PK_1", (slice, windowName))
  }

}
