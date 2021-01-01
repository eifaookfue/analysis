package jp.co.nri.nefs.tool.transport

import java.nio.file.{Path, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer
import scala.xml.XML

case class Artifact(pFile: Path, ivyFile: Path, site: String, organisation: String, module: String,
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

    val buffer = ListBuffer(s"""<!-- $pFile -->""")
    buffer += s"""<!-- $ivyFile -->"""
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



  def createArtifact(pFile: Path, ivyFile: Path): Artifact = {
    val ivyData = XML.loadFile(ivyFile.toFile)
    val info = ivyData \ "info"
    val organisation = info \@ "organisation"
    val module = info \@ "module"
    val revision = info \@ "revision"
    val sbtVersion = Option(info \ "@{http://ant.apache.org/ivy/extra}sbtVersion")
      .filter(_.nonEmpty).map(_.text)
    val scalaVersion = Option(info \ "@{http://ant.apache.org/ivy/extra}scalaVersion")
      .filter(_.nonEmpty).map(_.text)
    Artifact(pFile, ivyFile, "", organisation, module, revision, scalaVersion, sbtVersion)
  }

}
