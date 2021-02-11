package jp.co.nri.tcatool.ref.tse.reference.model

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class IndustryType17(
                        code: Int,
                        name: String,
                        updateTime: Timestamp
                        )

trait IndustryTyp17Component {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class IndustryType17s(tag: Tag) extends Table[IndustryType17](tag, "TSE_INDUSTRY_TYPE_17") {
    def code = column[Int]("CODE", O.PrimaryKey)
    def name = column[String]("NAME", O.Length(30))
    def updateTime = column[Timestamp]("UPDATE_TIME")
    def * = (code, name, updateTime) <> (IndustryType17.tupled, IndustryType17.unapply)

  }

}
