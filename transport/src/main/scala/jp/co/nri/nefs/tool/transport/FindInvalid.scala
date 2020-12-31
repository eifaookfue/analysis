package jp.co.nri.nefs.tool.transport

import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.util.FileUtils

import scala.collection.JavaConverters._

object FindInvalid extends LazyLogging {

  final val LOCAL_DIR = "local-dir"
  final val IS_DELETE_INVALID = "is-delete-invalid"
  private val config = ConfigFactory.load()

  import jp.co.nri.nefs.tool.util.config.RichConfig._
  private val localDir = Paths.get(config.getString(LOCAL_DIR, logger))
  private val isDeleteInvalid = config.getBoolean(IS_DELETE_INVALID, logger)

  def main(args: Array[String]): Unit = {
    val ivyFiles = FileUtils.autoClose(Files.walk(localDir)){ s =>
      s.iterator().asScala.filter(_.getFileName.toString == "ivy.xml").toList
    }
    logger.info("----------------------------------------------------------------------------------------------")
    logger.info("The following folders are the ones which should be placed at scala_X.XX originally but not")
    logger.info("----------------------------------------------------------------------------------------------")
    val nonScala = (for {
      ivyFile <- ivyFiles
      artifact = Artifact.createArtifact(ivyFile)
      if artifact.scalaVersion.isDefined
      folder <- nonScalaFolders(artifact)
    } yield folder).toSet
//    nonScala.foreach(p => logger.warn(s"folders = $p"))
    nonScala.foreach { p =>
      logger.warn(s"folders = $p")
      if (isDeleteInvalid)
        FileUtils.delete(p)
    }
    logger.info("----------------------------------------------------------------------------------------------")
    logger.info("The following folders are the ones which should NOT be placed at scala folder.")
    logger.info("----------------------------------------------------------------------------------------------")
    val scalaFolders = (for {
      ivyFile <- ivyFiles
      artifact = Artifact.createArtifact(ivyFile)
      if artifact.scalaVersion.isEmpty
      folder <- normalFolders(artifact)
    } yield folder).toSet
    scalaFolders.foreach { p =>
      logger.warn(s"folders = $p")
      if (isDeleteInvalid)
        FileUtils.delete(p)
    }
  }

  private def nonScalaFolders(artifact: Artifact): Seq[Path] = {
    val base = localDir.resolve(artifact.organisation).resolve(artifact.module)
    FileUtils.autoClose(Files.list(base)) { s =>
      s.iterator().asScala.filter(_.getFileName.toString != s"scala_${artifact.scalaVersion.get}")
        .toList
    }
  }

  private def normalFolders(artifact: Artifact): Seq[Path] = {
    val base = localDir.resolve(artifact.organisation).resolve(artifact.module)
    FileUtils.autoClose(Files.list(base)) { s =>
      s.iterator().asScala.filter(_.getFileName.toString.contains("scala"))
        .toList
    }
  }
}
