package jp.co.nri.nefs.tool.analytics.store.common

import com.typesafe.config.{Config, ConfigFactory}
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.{BasicProfile, DatabaseConfig}

class DefaultDatabaseConfigProvider extends DatabaseConfigProvider {

  val config: Config = ConfigFactory.load()
  val dbName: String = config.getString(DefaultDatabaseConfigProvider.DefaultDbName)

  override def get[P <: BasicProfile]: DatabaseConfig[P] =
    DatabaseConfig.forConfig[BasicProfile](dbName).asInstanceOf[DatabaseConfig[P]]
}

object DefaultDatabaseConfigProvider {
  final val DefaultDbName = "play.slick.db.default"
}