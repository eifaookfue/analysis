package jp.co.nri.nefs.tool.transport

import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.util.FileUtils

object DeleteCache extends LazyLogging {

  final val LOCAL_DIR = "local-dir"
  final val IS_DELETE_CACHE = "is-delete-cache"

  import jp.co.nri.nefs.tool.util.config.RichConfig._
  private val config = ConfigFactory.load()
  private val isDeleteCache = config.getBoolean(IS_DELETE_CACHE, logger)
  private val localDir = Paths.get(config.getString(LOCAL_DIR, logger))

  def main(args: Array[String]): Unit = {

    import Ivys._
    import Artifact._

    val folders = (for {
      pFile <- propertyFiles
      if isPublic(pFile)
      ivyFile <- ivyFileOption(pFile)
      at = createArtifact(ivyFile)
      localFile = localDir.resolve(at.organisation).resolve(at.module).resolve(at.revision)
      if Files.exists(localFile)
    } yield pFile.getParent).toSet

    folders.foreach { f =>
      logger.info(f.toString)
      if (isDeleteCache)
        FileUtils.delete(f)
    }

  }
}
