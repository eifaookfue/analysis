package jp.co.nri.nefs.tool.analytics.store.common

import com.typesafe.config.{Config, ConfigFactory}
import play.api.db.slick.{DatabaseConfigProvider, SlickModule}
import slick.basic.{BasicProfile, DatabaseConfig}

class DefaultDatabaseConfigProvider extends DatabaseConfigProvider {

  val config: Config = ConfigFactory.load()
  // slick.dbs.default
  val dbName: String = config.getString(SlickModule.DbKeyConfig) +
    "." + config.getString(SlickModule.DefaultDbName)

  override def get[P <: BasicProfile]: DatabaseConfig[P] =
    DatabaseConfig.forConfig[BasicProfile](dbName).asInstanceOf[DatabaseConfig[P]]
}
