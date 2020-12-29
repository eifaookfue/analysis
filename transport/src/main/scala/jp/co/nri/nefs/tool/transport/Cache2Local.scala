package jp.co.nri.nefs.tool.transport

import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.util.FileUtils

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.Properties
import scala.xml.XML

case class Artifact(file: Path, site: String, organisation: String, module: String,
                    revision: String, scalaVersion:Option[String], sbtVersion: Option[String]) {

  def from: String = "normal"

  def to: String = "local-scala"

  def buildFileName: String = {
    Array(organisation, module, revision,
      sbtVersion.getOrElse("null"), scalaVersion.getOrElse("null")
    ).mkString("_") + ".xml"
  }

  def buildFileBuffer: ListBuffer[String] = {
    import Artifact._

    val buffer = ListBuffer(s"""<!-- $file -->""")
    buffer += """<project name="localrepository" default="install""""
    buffer +=  s"""\txmlns:ivy="antlib:org.apache.ivy.ant">"""
    buffer += s"""\t<property name="ivy.default.ivy.user.dir" value="$ivyDir" />"""
    buffer += s"""\t<property name="my.settings.dir" value="$ivySettings" />"""
    buffer += s"""\t<property name="ivy.settings.file" value="$${my.settings.dir}\\ivysettings.xml" />"""
    buffer += s"""\t<target name=\"install\" description=\"--> install modules to localrepository\" >"""
    buffer += s"""\t\t<ivy:install organisation="$organisation" module="$module""""
    buffer += s"""\t\t\trevision="$revision" transitive="true" overwrite="true" from="$from""""
    buffer += s"""\t\t\tto="$to" />"""
    buffer += "\t</target>"
    buffer += "</project>"
    buffer
  }
}

object Artifact extends LazyLogging {
  import jp.co.nri.nefs.tool.util.config.RichConfig._
  final val IVY_DIR = "ivy-dir"
  final val IVY_SETTINGS_DIR = "ivy-settings"
  private val config = ConfigFactory.load()
  private val ivyDir = Paths.get(config.getString(IVY_DIR, logger)).toString
  private val ivySettings = Paths.get(config.getString(IVY_SETTINGS_DIR, logger)).toString
  final val MAVEN_SITE = """.*repo1.maven.org/maven2/(.*)""".r
  final val HTTP_MAVEN = "https\\://" + MAVEN_SITE



  def createArtifact(ivyFile: Path): Artifact = {
    val ivyData = XML.loadFile(ivyFile.toFile)
    val info = ivyData \ "info"
    val organisation = info \@ "organisation"
    val module = info \@ "module"
    val revision = info \@ "revision"
    val sbtVersion = Option(info \ "@{http://ant.apache.org/ivy/extra}sbtVersion")
      .filter(_.nonEmpty).map(_.text)
    val scalaVersion = Option(info \ "@{http://ant.apache.org/ivy/extra}scalaVersion")
      .filter(_.nonEmpty).map(_.text)
    println(s"organisation = $organisation")
    Artifact(ivyFile, "", organisation, module, revision, scalaVersion, sbtVersion)
  }

}

object Cache2Local extends LazyLogging{

  final val CACHE_DIR = "cache-dir"
  final val PROPERTIES = "properties"
  final val BUILD_FILE_DIR = "build-file-dir"
  final val HTTPS = "https"

  import jp.co.nri.nefs.tool.util.config.RichConfig._

  private val config = ConfigFactory.load()
  private val cacheDir = Paths.get(config.getString(CACHE_DIR, logger))
  private val buildFileDir = Paths.get(config.getString(BUILD_FILE_DIR, logger))

  def main(args: Array[String]): Unit = {

    if (Files.exists(buildFileDir)) {
      logger.info(s"Deleting $buildFileDir")
      FileUtils.delete(buildFileDir)
      logger.info("Done.")
    }
    Files.createDirectories(buildFileDir)
    logger.info(s"Created $buildFileDir")

    val files = FileUtils.autoClose(Files.walk(cacheDir))(_.iterator()
      .asScala.filter(_.getFileName.toString.endsWith(PROPERTIES)).toList)
    for {
      file <- files
      ivyFile <- ivyFileOption(file)
      artifact = Artifact.createArtifact(ivyFile)
    } {
      logger.info(s"${buildFileDir.resolve(artifact.buildFileName)}:")
      val s = Properties.lineSeparator + artifact.buildFileBuffer.mkString(Properties.lineSeparator)
      logger.info(s)
      val buildFile = buildFileDir.resolve(artifact.buildFileName)
      Files.write(buildFile, artifact.buildFileBuffer.asJava)
      AntExecutor(buildFile).execute()
    }
  }

  def ivyFileOption(pFile: Path): Option[Path] = {
    val lines = Files.readAllLines(pFile).asScala.toList
    lines.collectFirst {
      case line if line.contains(HTTPS) && !line.contains("sources") && toIvyFile(pFile).isDefined
      => toIvyFile(pFile).get
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

case class AntExecutor(path: Path) extends LazyLogging {
  import AntExecutor._
  def execute(): Unit = {
    val execResult = execute(Seq("cmd", "/c", antBinary, "-f", path.toString))
    if (execResult.result == 0) {
      logger.info(s"$path execution succeeded.")
    } else {
      logger.error(s"$path execution failed.")
    }
    execResult.out.foreach(s => logger.info(s))
    execResult.err.foreach(s => logger.error(s))

  }

  private def execute(cmd: Seq[String]): ExecResult = {
    import scala.sys.process._

    val out = ListBuffer[String]()
    val err = ListBuffer[String]()

    val processLogger = ProcessLogger(
      (o: String) => out += o,
      (e: String) => err += e
    )

    val r = Process(cmd) ! processLogger

    ExecResult(r, out.toList, err.toList)
  }
}

object AntExecutor extends LazyLogging {
  final val ANT_BINARY = "ant-binary"
  private val config = ConfigFactory.load()
  import jp.co.nri.nefs.tool.util.config.RichConfig._
  private val antBinary = config.getString(ANT_BINARY, logger)
}

case class ExecResult(result: Int, out: List[String], err: List[String])
