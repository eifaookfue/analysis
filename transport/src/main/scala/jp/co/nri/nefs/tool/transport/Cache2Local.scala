package jp.co.nri.nefs.tool.transport
import java.nio.file.{Files, Path, Paths}
import java.util.Date
import java.util.stream.Collectors

import jp.co.nri.nefs.common.util.config.ConfigurationFactory
import jp.co.nri.nefs.common.util.config.tree.{ConfigurationNode, TreeConfiguration}
import jp.co.nri.nefs.tool.transport.Cache2Local.getAttributes

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

case class ExecResult(result: Int, out: List[String], err: List[String])

case class BuildfileCreator(cachedir: Path, outputdir: Path){
  def create(): Unit = {
    val template = Paths.get("D:\\Apl\\analysis\\transport\\src\\main\\resources\\template.xml")
    Files.createDirectories(outputdir)
    val paths = Files.walk(cachedir).filter(_.toFile.isFile).collect(Collectors.toList()).asScala
    for (path <- paths;
         fileName = path.getFileName.toFile.toString
         if fileName.endsWith("xml") ){
      val url = path.toUri.toURL
      ConfigurationFactory.load(url)
      val config = ConfigurationFactory.getConfiguration().asInstanceOf[TreeConfiguration]
      val root = config.getRootNode
      val info = root.getChild("info")
      val (organisation, module, revision, sbtVersionOpt, scalaVersionOpt) = getAttributes(info)
      val outName = Array(organisation, module, revision,
        sbtVersionOpt.getOrElse("null"), scalaVersionOpt.getOrElse("null")
      ).mkString("_") + ".xml"
      val outputPath = outputdir.resolve(outName)

      val allLines = Files.readAllLines(template).asScala

      val newLines = for (line <- allLines) yield {
        val s1 = line.replace("_ORGANISATION_", organisation)
        val s2 = s1.replace("_MODULE_", module)
        val s3 = s2.replace("_REVISION_", revision)
        val s4 = sbtVersionOpt match {
          case Some(sbtVersion) =>
            val scalaVersion = scalaVersionOpt.get
            val str0 = ""
            val str1 = "\t<property name=\"scalaVersion\" value=\"" + scalaVersion + "\" />"
            val str2 = "\t<property name=\"sbtVersion\" value=\"" + sbtVersion + "\" />"
            val str3 = "\t<property name=\"classifier\" value=\"" + revision + "\" />"
            val strs = for ((s, index) <- organisation.split("\\.").zipWithIndex) yield
              "\t<property name=\"organisation" + index + "\" value=\"" + s + "\" />"
            val str = Array(Array(str0),Array(str1),Array(str2), Array(str3), strs)
              .flatten.mkString(sys.props("line.separator"))
            s3.replace("_REPLACESTR_", str)
          case None =>
            s3.replace("_REPLACESTR_", "")
        }
        val s5 = sbtVersionOpt match {
          case Some(_) =>
            s4.replace("_FROM_", "public2")
          case None =>
            s4.replace("_FROM_", "public")
        }
        val s6 = sbtVersionOpt match {
          case Some(_) =>
            s5.replace("_TO_", "local2")
          case None =>
            s5.replace("_TO_", "local")
        }
        s6
      }
      val localPath = cachedir.getParent.resolve(Paths.get("local"
        ,organisation, module, revision, "ivys", "ivy.xml"))
      if (Files.notExists(localPath))
        Files.write(outputPath, newLines.asJava)
    }

  }
}

case class AntExecutor(){

  val errorBufferPath: ListBuffer[Path] = ListBuffer[Path]()
  val errorBufferStr: ListBuffer[String] = ListBuffer[String]()

  def exec(cmd: Seq[String]):ExecResult = {

    import scala.sys.process._

    val out = ListBuffer[String]()
    val err = ListBuffer[String]()

    val logger = ProcessLogger(
      (o: String) => out += o,
      (e : String) => err += e
    )

    val r = Process(cmd) ! logger

    ExecResult(r, out.toList, err.toList)

  }


  def execute(path: Path): Unit = {
    if (path.toFile.isDirectory){
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

  val usage = """
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
    } else if (cachedir.isDefined && (execdir.isDefined || execfile.isDefined)){
      true
    } else if (cachedir.isDefined && outputdir.isEmpty) {
      true
    } else if (execdir.isDefined && execfile.isDefined){
      true
    } else {
      false
    }
    if (err){
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
    if (isBuild){
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

  def getAttributes(node: ConfigurationNode): (String, String, String, Option[String], Option[String]) = {
    val organisation = node.getAttribute("organisation").getValue
    val module = node.getAttribute("module").getValue
    val revision = node.getAttribute("revision").getValue
    val sbtVersion = Option(node.getAttribute("e:sbtVersion").getValue).filter(_.nonEmpty)
    val scalaVersion = Option(node.getAttribute("e:scalaVersion").getValue).filter(_.nonEmpty)
    (organisation, module, revision, sbtVersion, scalaVersion)
  }



}