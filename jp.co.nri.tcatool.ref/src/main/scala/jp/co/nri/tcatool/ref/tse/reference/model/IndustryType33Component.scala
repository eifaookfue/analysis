package jp.co.nri.tcatool.ref.tse.reference.model

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class IndustryType33(
                        code: Int,
                        name: String,
                        updateTime: Timestamp
                        )

trait IndustryTyp33Component {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class IndustryType33s(tag: Tag) extends Table[IndustryType33](tag, "TSE_INDUSTRY_TYPE_33") {
    def code = column[Int]("CODE", O.PrimaryKey)
    def name = column[String]("NAME", O.Length(30))
    def updateTime = column[Timestamp]("UPDATE_TIME")
    def * = (code, name, updateTime) <> (IndustryType33.tupled, IndustryType33.unapply)

  }

}
