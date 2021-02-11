package jp.co.nri.tcatool.ref.tse.reference.model

import java.sql.Timestamp
import java.time.LocalDate

import jp.co.nri.tcatool.ref.reference.model.{EProductType, Mapper}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class TSEInstrument(
                          symbol: String,
                          baseDate: LocalDate,
                          name: String,
                          productType: Option[EProductType],
                          //TODO Need to be changed to Enum
                          industryType33: Option[Int],
                          industryType17: Option[Int],
                          TOPIXCategory: Option[Int],
                          updateTime: Timestamp
                        )


trait TSEInstrumentComponent extends Mapper {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._
  
  class TSEInstruments(tag: Tag) extends Table[TSEInstrument](tag, "TSE_INSTRUMENT") {
    def symbol = column[String]("SYMBOL", O.Length(10), O.PrimaryKey)
    def baseDate = column[LocalDate]("BASE_DATE")
    def name = column[String]("NAME", O.Length(200))
    def productType = column[Option[EProductType]]("PRODUCT_TYPE", O.Length(30))
    def industryType33 = column[Option[Int]]("INDUSTRY_TYPE_33")
    def industryType17 = column[Option[Int]]("INDUSTRY_TYPE_17")
    def TOPIXCategory = column[Option[Int]]("TOPIX_CATEGORY")
    def updateTime = column[Timestamp]("UPDATE_TIME")

    def * = (symbol, baseDate, name, productType, industryType33, industryType17,
      TOPIXCategory, updateTime) <> (TSEInstrument.tupled, TSEInstrument.unapply)
  }
  
}
