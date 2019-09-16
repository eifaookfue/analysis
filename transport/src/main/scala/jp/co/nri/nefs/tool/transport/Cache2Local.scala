package jp.co.nri.nefs.tool.transport
import java.nio.file.{Files, Path, Paths}
import java.util.Date
import java.util.stream.Collectors
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object Regexs {
  case class Artifact(site: String, organisation: String, module: String,
                      revision: String, artifact: String, ext: String,
                      scalaVersion:Option[String], sbtVersion: Option[String]) {

    def outName: String = {
      Array(organisation, module, revision,
        sbtVersion.getOrElse("null"), scalaVersion.getOrElse("null")
      ).mkString("_") + ".xml"
    }
    def getFrom: String = {
      "normal"
    }
    def getTo: String = {
      scalaVersion match {
        case Some(_) => "local-scala"
        case None => "local"
      }
    }
  }
  def of(site: String, organisationSlash: String, moduleOrg: String,
         revision: String, artifact: String, ext: String,
         scalaInfo:Option[String], sbtInfo: Option[String]): Option[Artifact] = {
    //sbt-sonatype_2.12_1.0 これはOK
    //error_prone_annotations これはNG
    //{1,2}で直前の1文字以上2文字以下
    lazy val regex = """(.*)_([0-9]{1,2}\.[0-9]{1,2})_([0-9]{1,2}\.[0-9]{1,2})""".r
    if (artifact.equals("ivy"))
      None
    else {
      val (module, scalaVersion, sbtVersion) = {
        val scalaVersion = scalaInfo.map(_.replace("scala_",""))
        val sbtVersion = sbtInfo.map(_.replace("sbt_",""))
        if (scalaVersion.isDefined)
          (moduleOrg, scalaVersion, sbtVersion)
        else {
          moduleOrg match {
            case regex(m, scalaV, sbtV) => (m, Some(scalaV), Some(sbtV))
            case _ => (moduleOrg, None, None)
          }
        }
      }
      val organisation = organisationSlash.replace("/",".")
      Some(Artifact(site, organisation, module, revision, artifact, ext, scalaVersion, sbtVersion))
    }
  }
  def getArtifact(line: String): Option[Artifact] = {
    lazy val mavenExOrg3 = """(.*)https\\:\/\/(repo1.maven.org\/maven2)\/(.*\/.*\/.*)\/(.*)\/(.*)\/(.*)\.(.*)""".r
    lazy val mavenExOrg2 = """(.*)https\\:\/\/(repo1.maven.org\/maven2)\/(.*\/.*)\/(.*)\/(.*)\/(.*)\.(.*)""".r
    lazy val mavenExOrg1 = """(.*)https\\:\/\/(repo1.maven.org\/maven2)\/(.*)\/(.*)\/(.*)\/(.*)\.(.*)""".r
    //https\://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/com.github.gseitz/sbt-release/
    // scala_2.12/sbt_1.0/1.0.7/jars/sbt-release.jar
    lazy val scalasbtEx = """(.*)https\\:\/\/(repo.scala-sbt.org\/scalasbt\/sbt-plugin-releases)\/(.*)\/(.*)\/(scala.*)\/(sbt.*)\/(.*)\/(.*)\/(.*)\.(.*)""".r
    lazy val scalasbtNoVersionEx = """(.*)https\\:\/\/(repo.scala-sbt.org\/scalasbt\/sbt-plugin-releases)\/(.*)\/(.*)\/(.*)\/(.*)\/(.*)\.(.*)""".r
    lazy val httpEx = """(.*)https\\:\/\/(.*)""".r
    line match {
      case scalasbtEx(_, site, organisationSlash, module, scalaInfo, sbtInfo, revision, _, artifact, ext) =>
        of(site, organisationSlash, module, revision, artifact, ext, Some(scalaInfo), Some(sbtInfo))
      case scalasbtNoVersionEx(_, site, organisationSlash, module, revision, _, artifact, ext) =>
        of(site, organisationSlash, module, revision, artifact, ext, None, None)
      case mavenExOrg3(_, site, organisationSlash, module, revision, artifact, ext) =>
        of(site, organisationSlash, module, revision, artifact, ext, None, None)
      case mavenExOrg2(_, site, organisationSlash, module, revision, artifact, ext) =>
        of(site, organisationSlash, module, revision, artifact, ext, None, None)
      case mavenExOrg1(_, site, organisationSlash, module, revision, artifact, ext) =>
        of(site, organisationSlash, module, revision, artifact, ext, None, None)
      case httpEx(_,_) => throw new java.lang.RuntimeException(s"$line dose not match any expression.")
      case _ => None
    }
  }
}

case class ExecResult(result: Int, out: List[String], err: List[String])

case class BuildfileCreator(cachedir: Path, outputdir: Path) {

  import Regexs._

  def create(): Unit = {
    Files.createDirectories(outputdir)
    val paths = Files.walk(cachedir).filter(_.toFile.isFile).collect(Collectors.toList()).asScala
    for (path <- paths;
         fileName = path.getFileName.toFile.toString
         if fileName.endsWith("properties")) {
      val lines = Files.readAllLines(path).asScala
      for (line <- lines; artifact <- getArtifact(line)) {
        val buffer = ListBuffer("<project name=\"localrepository\" default=\"install\"")
        buffer += "\txmlns:ivy=\"antlib:org.apache.ivy.ant\">"
        buffer += "\t<property name=\"ivy.default.ivy.user.dir\" value=\"d:\\Apl\\.ivy2\" />"
        buffer += "\t<property name=\"my.settings.dir\" value=\"D:\\Apl\\analysis\\ivysettings\" />"
        buffer += "\t<property name=\"ivy.settings.file\" value=\"${my.settings.dir}\\ivysettings.xml\" />"
        for (sbtVersion <- artifact.sbtVersion; scalaVersion <- artifact.scalaVersion) {
          buffer += "\t<property name=\"scalaVersion\" value=\"" + scalaVersion + "\" />"
          buffer += "\t<property name=\"sbtVersion\" value=\"" + sbtVersion + "\" />"
          buffer += "\t<property name=\"classifier\" value=\"" + artifact.revision + "\" />"
          buffer ++= (for ((s, index) <- artifact.organisation.split("\\.").zipWithIndex) yield
            "\t<property name=\"organisation" + index + "\" value=\"" + s + "\" />").toList
        }
        buffer += "\t<target name=\"install\" description=\"--> install modules to localrepository\">"
        buffer += "\t\t<ivy:install organisation=\"" + artifact.organisation + "\" module=\"" + artifact.module + "\""
        buffer += "\t\t\trevision=\"" + artifact.revision + "\" transitive=\"true\" overwrite=\"true\" from=\"" + artifact.getFrom + "\""
        buffer += "\t\t\tto=\"" + artifact.getTo + "\" />"
        buffer += "\t</target>"
        buffer += "</project>"
        Files.write(outputdir.resolve(artifact.outName), buffer.asJava)
      }
    }
  }
}
case class AntExecutor() {

  val errorBufferPath: ListBuffer[Path] = ListBuffer[Path]()
  val errorBufferStr: ListBuffer[String] = ListBuffer[String]()

  def exec(cmd: Seq[String]): ExecResult = {

    import scala.sys.process._

    val out = ListBuffer[String]()
    val err = ListBuffer[String]()

    val logger = ProcessLogger(
      (o: String) => out += o,
      (e: String) => err += e
    )

    val r = Process(cmd) ! logger

    ExecResult(r, out.toList, err.toList)

  }


  def execute(path: Path): Unit = {
    if (path.toFile.isDirectory) {
      Files.list(path).filter(_.getFileName.toFile.toString.endsWith("xml")).forEach(execute(_))
      return
    }
    val result = exec(Seq("cmd", "/c", "D:\\Apl\\apache-ant-1.10.5\\bin\\ant", "-f", path.toString))
    println("-----Standard output-----")
    for (str <- result.out) println(str)

    println(result.out)
    println("-----Standard err-----")
    var isBuildFailed = false
    for (str <- result.err) {
      if (str.contains("BUILD FAILED"))
        isBuildFailed = true
      println(str)
      errorBufferStr += str
    }
    if (isBuildFailed) errorBufferPath += path
  }
}

object Cache2Local {
  type OptionMap = Map[Symbol, String]

  val usage =
    """
      Usage: jp.co.nri.nefs.tool.transport.Cache2Local [--cachedir dir --outputdir dir|[--execdir dir|--execfile file]]
      """

  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    list match {
      case Nil => map
      case "--cachedir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("cachedir") -> value), tail)
      case "--outputdir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("outputdir") -> value), tail)
      case "--execdir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("execdir") -> value), tail)
      case "--execfile" :: value :: tail =>
        nextOption(map ++ Map(Symbol("execfile") -> value), tail)
      case _ => println("Unknown option")
        println(usage)
        throw new java.lang.IllegalArgumentException
    }
  }

  def getOption(options: OptionMap): (Boolean, Path, Path) = {
    val cachedir = options.get(Symbol("cachedir"))
    val outputdir = options.get(Symbol("outputdir"))
    val execdir = options.get(Symbol("execdir"))
    val execfile = options.get(Symbol("execfile"))
    val err = if (cachedir.isEmpty && execdir.isEmpty && execfile.isEmpty) {
      true
    } else if (cachedir.isDefined && (execdir.isDefined || execfile.isDefined)) {
      true
    } else if (cachedir.isDefined && outputdir.isEmpty) {
      true
    } else if (execdir.isDefined && execfile.isDefined) {
      true
    } else {
      false
    }
    if (err) {
      println(usage)
      throw new java.lang.IllegalArgumentException()
    }
    if (cachedir.isDefined)
      (true, Paths.get(cachedir.get), Paths.get(outputdir.get))
    else {
      val path = if (execdir.isDefined) Paths.get(execdir.get) else Paths.get(execfile.get)
      (false, path, null)
    }
  }

  def main(args: Array[String]): Unit = {
    //val uri = Cache2Local.getClass.getClassLoader.getResource("template.xml").toURI
    //val template = Paths.get(uri)

    val options = nextOption(Map(), args.toList)
    val (isBuild, path1, path2) = getOption(options)
    if (isBuild) {
      BuildfileCreator(path1, path2).create()
    } else {
      val outPathParent = if (path1.toFile.isDirectory) path1 else path1.getParent
      val df = new java.text.SimpleDateFormat("yyyyMMddHHmmss")
      val outFileNameBase = df.format(new Date())
      val outFileNamePath = outFileNameBase + "Path.txt"
      val outFileNameStr = outFileNameBase + "Str.txt"
      //val outFileName = "%tY% <tm% <td %<tH %<tM %<tS" format new Date
      val outPath = outPathParent.resolve(outFileNamePath)
      val executor = AntExecutor()
      executor.execute(path1)
      val buffer = for (line <- executor.errorBufferPath) yield line.toString
      Files.write(outPath, buffer.asJava)
      val outStrPath = outPathParent.resolve(outFileNameStr)
      val bufferStr = for (line <- executor.errorBufferStr) yield line.toString
      Files.write(outStrPath, bufferStr.asJava)
    }
  }
}

