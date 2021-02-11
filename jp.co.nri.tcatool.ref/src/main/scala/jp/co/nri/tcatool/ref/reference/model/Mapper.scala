package jp.co.nri.tcatool.ref.reference.model

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait Mapper {

  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  implicit val sessionTypeType: BaseColumnType[ESessionType] = MappedColumnType.base[ESessionType, String](
    _.toString,
    ESessionType.valueOf
  )

  implicit val bsTypeType: BaseColumnType[EBSType] = MappedColumnType.base[EBSType, String](
    _.toString,
    EBSType.valueOf
  )

  implicit val marketType: BaseColumnType[EMarket] = MappedColumnType.base[EMarket, String](
    _.toString,
    EMarket.valueOf
  )

  implicit val activeFlagType: BaseColumnType[EActiveFlag] = MappedColumnType.base[EActiveFlag, String](
    _.toString,
    EActiveFlag.valueOf
  )

  implicit val productTypeMapper: BaseColumnType[EProductType] = MappedColumnType.base[EProductType, String](
    _.toString,
    EProductType.valueOf
  )

}
