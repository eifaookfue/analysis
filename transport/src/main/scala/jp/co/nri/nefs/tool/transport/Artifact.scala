package jp.co.nri.nefs.tool.transport

import java.nio.file.{Path, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.xml.{PrettyPrinter, XML}

case class Artifact(ivyFile: Path, site: String, organisation: String, module: String,
                    revision: String, scalaVersion:Option[String], sbtVersion: Option[String],
                    pFile: Option[Path] = None) {

  def withPFile(pFile: Path): Artifact = {
    copy(pFile = Some(pFile))
  }

  def from: String = "normal"

  def to: String = "local-scala"

  def buildFileName: String = {
    Array(organisation, module, revision,
      sbtVersion.getOrElse("null"), scalaVersion.getOrElse("null")
    ).mkString("_") + ".xml"
  }

  def buildFile: String = {
    import Artifact._

    val xml = <project name="localrepository" default="install"
             xmlns:ivy="antlib:org.apache.ivy.ant">
      <property name="ivy.default.ivy.user.dir" value={ivyDir} />
      <property name="my.settings.dir" value={ivySettings} />
      <property name="ivy.settings.file" value="${my.settings.dir}\ivysettings.xml" />
      <target name="install" description="--> install modules to localrepository" >
        <ivy:install organisation={organisation} module={module}
                     revision={revision} transitive="true" overwrite="true" from={from}
                     to={to} />
      </target>
    </project>
    val pp = new PrettyPrinter(100, 4)
    pp.format(xml)
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
    Artifact(ivyFile, "", organisation, module, revision, scalaVersion, sbtVersion)
  }

}
