package jp.co.nri.nefs.tool.transport

import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.util.FileUtils

import scala.collection.JavaConverters._

object Ivys extends LazyLogging {

  final val PROPERTIES = "properties"
  final val HTTPS = "https"
  final val SOURCES = "sources"
  final val CACHE_DIR = "cache-dir"
  final val LOCAL_DIR = "local-dir"

  import jp.co.nri.nefs.tool.util.config.RichConfig._

  private val config = ConfigFactory.load()
  private val cacheDir = Paths.get(config.getString(CACHE_DIR, logger))

  def propertyFiles: List[Path] = FileUtils.autoClose(Files.walk(cacheDir))(_.iterator()
    .asScala.filter(_.getFileName.toString.endsWith(PROPERTIES)).toList)

  /**
    * Returns if this ivy module was got from public.
    * @param pFile ivy properties file
    * @return
    */
  def isPublic(pFile: Path): Boolean = {
    val lines = Files.readAllLines(pFile).asScala.toList
    lines.exists(l => l.contains(HTTPS) && !l.contains(SOURCES))
  }

  def ivyFileOption(pFile: Path): Option[Path] = {
    if (isPublic(pFile)) {
      toIvyFile(pFile)
    } else {
      None
    }
  }

  private def toIvyFile(file: Path): Option[Path] = {
    val fileName = file.getFileName.toString
      .replace("ivydata", "ivy").replace("properties", "xml")
    val path = file.getParent.resolve(fileName)
    if (Files.exists(path))
      Some(path)
    else {
      logger.error(s"$path didn't exist.")
      None
    }
  }

}
