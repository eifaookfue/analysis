package jp.co.nri.nefs.tool.compact

import java.nio.file.{Path, Paths}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.util.config.RichConfig._

object Compacts extends  LazyLogging {

  private final val BASE = "base"
  private final val PROJECTS = "projects"
  private final val FROM_OUT_DIR = "from-out-dir"
  private final val KEYWORD_BEGIN = "keyword-begin"
  private final val KEYWORD_END = "keyword-end"
  private final val TO_OUT_DIR = "to-out-dir"
  private val config = ConfigFactory.load().getConfig("jp.co.nri.nefs.tool.compact")

  val base: Path = Paths.get(config.getString(BASE, logger))
  val projects: List[String] = config.getStringList(PROJECTS, logger)
  val fromOut: Path = Paths.get(config.getString(FROM_OUT_DIR, logger))
  val begin: String = config.getString(KEYWORD_BEGIN)
  val end: String = config.getString(KEYWORD_END)
  val toOut: Path = Paths.get(config.getString(TO_OUT_DIR, logger))

}
