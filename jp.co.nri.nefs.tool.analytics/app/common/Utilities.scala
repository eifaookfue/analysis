package common

import java.text.SimpleDateFormat

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.db.slick.SlickModule
import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._
import scala.util.{Success, Try}

@Singleton()
class Utilities @Inject() (config: Configuration) (implicit executionContext: ExecutionContext) {

  // slick.dbs.default
  val dbName: String = config.underlying.getString(SlickModule.DbKeyConfig) +
    "." + config.underlying.getString(SlickModule.DefaultDbName)

  val conf: Config = config.underlying.getConfig(dbName)
  val dateFormatters: Seq[Map[String, AnyRef]] = conf.getObjectList("dateFormatters").asScala
    .map(_.unwrapped()).map(_.asScala.toMap)

  /*
  Returns RDBFormat whose counter party of SimpleFormat is valid for the argument
  */
  def formatter(value: String): Option[String] = (for {
    o <- dateFormatters
    (simpleFmt, rdbFmt) <- o
    format = new SimpleDateFormat(simpleFmt)
    fmt = Try {
      format.parse(value)
      rdbFmt.toString
    }
  } yield fmt).collectFirst { case Success(v) => v }

}