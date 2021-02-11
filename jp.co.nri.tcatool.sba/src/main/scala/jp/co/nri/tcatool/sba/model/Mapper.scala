package jp.co.nri.tcatool.sba.model

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait Mapper {

  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  implicit val orderTypeType: BaseColumnType[EOrderType] = MappedColumnType.base[EOrderType, String](
    _.toString,
    EOrderType.valueOf
  )

  implicit val historyTypeType: BaseColumnType[EHistoryType] = MappedColumnType.base[EHistoryType, String](
    _.toString,
    EHistoryType.valueOf
  )



}
