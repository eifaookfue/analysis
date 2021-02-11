package jp.co.nri.tcatool.ref.reference.model

import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class Broker(
                   brokerId: Int,
                   brokerName: String,
                   abbreviation: String,
                   updateTime: Timestamp
                 )

trait BrokerComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class Brokers(tag: Tag) extends Table[Broker](tag, "TCA_BROKER") {
    def brokerId = column[Int]("BROKER_ID", O.PrimaryKey)
    def brokerName = column[String]("BROKER_NAME", O.Length(100))
    def abbreviation = column[String]("ABBREVIATION", O.Length(10))
    def updateTime = column[Timestamp]("UPDATE_TIME" ,SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def * =
      (brokerId, brokerName, abbreviation, updateTime) <> (Broker.tupled, Broker.unapply)
  }

}