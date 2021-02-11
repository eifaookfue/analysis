package jp.co.nri.tcatool.ref.tse.reference.model

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class TOPIXCategory(
                        code: Int,
                        name: String,
                        updateTime: Timestamp
                        )

trait TOPIXCategoryComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class TOPIXCategories(tag: Tag) extends Table[TOPIXCategory](tag, "TSE_TOPIX_CATEGORY") {
    def code = column[Int]("CODE", O.PrimaryKey)
    def name = column[String]("NAME", O.Length(30))
    def updateTime = column[Timestamp]("UPDATE_TIME")
    def * = (code, name, updateTime) <> (TOPIXCategory.tupled, TOPIXCategory.unapply)

  }

}
