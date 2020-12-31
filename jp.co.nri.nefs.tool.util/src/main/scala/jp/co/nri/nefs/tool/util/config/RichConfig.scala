package jp.co.nri.nefs.tool.util.config

import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import scala.collection.JavaConverters._
import scala.language.implicitConversions

class RichConfig(config: Config) {

  def getString(s: String, logger: Logger): String = getAndLogging(s, logger, config.getString)

  def getInt(s: String, logger: Logger): Int = getAndLogging(s, logger, config.getInt)

  def getBoolean(s: String, logger: Logger): Boolean = getAndLogging(s, logger, config.getBoolean)

  def getStringList(s: String, logger: Logger): List[String] =
    getAndLogging(s, logger, config.getStringList(_).asScala.toList)

  def getMapping[T](s: String): Map[String, T] = {
    config.getObjectList(s).asScala
      .map(_.unwrapped()).map(_.asScala).map(v => (v.head._1, v.head._2.asInstanceOf[T])).toMap
  }

  def getMapping[T](s: String, logger: Logger): Map[String, T] = getAndLogging(s, logger, v => getMapping(v))

  private def getAndLogging[T](s: String, logger: Logger, f: String => T): T = {
    val value = f(s)
    logging(s, value, logger)
    value
  }

  private def logging(s: String, o: Any, logger: Logger): Unit = {
    logger.info(s"loaded $s -> $o")
  }
}

object RichConfig {
  implicit def configToRichConfig(config: Config): RichConfig = new RichConfig(config)
}