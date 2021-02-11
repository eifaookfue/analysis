package jp.co.nri.tcatool.ref.reference.model

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class Strategy(
                     strategyName: String,
                     strategyType: String,
                     updateTime: Timestamp
                   )

trait StrategyComponent extends Mapper {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class Strategies(tag: Tag) extends Table[Strategy](tag, "TCA_STRATEGY") {
    def strategyName = column[String]("STRATEGY_NAME", O.Length(100), O.PrimaryKey)
    def strategyType = column[String]("STRATEGY_TYPE", O.Length(30))
    def updateTime  = column[Timestamp]("UPDATE_TIME")
    def * =
      (strategyName, strategyType, updateTime) <> (Strategy.tupled, Strategy.unapply)
  }

}
