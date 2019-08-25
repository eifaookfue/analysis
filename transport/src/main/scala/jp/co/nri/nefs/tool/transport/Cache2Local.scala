package jp.co.nri.nefs.tool.transport
import java.nio.file.{Files, Path, Paths}
import java.util.stream.Collectors

import jp.co.nri.nefs.common.util.config.ConfigurationFactory
import jp.co.nri.nefs.common.util.config.tree.{ConfigurationNode, TreeConfiguration}

import scala.collection.JavaConverters._

object Cache2Local {
  type OptionMap = Map[Symbol, String]

  val usage = """
        Usage: jp.co.nri.nefs.tool.transport.Cache2Local --cachedir dir --outputdir dir
        """
  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    list match {
      case Nil => map
      case "--cachedir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("cachedir") -> value), tail)
      case "--outputdir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("outputdir") -> value), tail)
      case _ => println("Unknown option")
        println(usage)
        throw new java.lang.IllegalArgumentException
    }
  }
  def getOption(options: OptionMap): (Path, Path) = {
    val cachedir = options.get(Symbol("cachedir"))
    val outputdir = options.get(Symbol("outputdir"))
    if (cachedir.isEmpty || outputdir.isEmpty) {
      println(usage)
      throw new java.lang.IllegalArgumentException
    }
    (Paths.get(cachedir.get), Paths.get(outputdir.get))
  }

  def main(args: Array[String]): Unit = {
    //val uri = Cache2Local.getClass.getClassLoader.getResource("template.xml").toURI
    //val template = Paths.get(uri)
    val template = Paths.get("D:\\Apl\\analysis\\transport\\src\\main\\resources\\template.xml")
    val options = nextOption(Map(), args.toList)
    val (cachedir, outputdir) = getOption(options)
    val paths = Files.walk(cachedir).filter(_.toFile.isFile).collect(Collectors.toList()).asScala
    //val paths = Files.walk(cachedir).filter(_.getFileName.toFile.toString == "ivy-0.5.1.xml").collect(Collectors.toList()).asScala
    for (path <- paths;
        fileName = path.getFileName.toFile.toString
        if fileName.endsWith("xml")){
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

      /*val outName = Array(outputdir.toString, organisationOpt.getOrElse("null"),
        moduleOpt.getOrElse("null"), revisionOpt.getOrElse("null"),
        sbtVersionOpt.getOrElse("null"), scalaVersionOpt.getOrElse("null")
      ).mkString("_")
      val fileOutputStream = new FileOutputStream(outName, false)
      val writer = new OutputStreamWriter(fileOutputStream, "UTF-8")*/

      Files.createDirectories(outputdir)

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
      Files.write(outputPath, newLines.asJava)


      /*Files.lines(template).map(line => {
        val opt = for (organisation <- organisationOpt; module <- moduleOpt; revision <- revisionOpt) yield {
          val s1 = line.replace("_ORGANISATION_", organisation)
          val s2 = s1.replace("_MODULE_", module)
          s2.replace("_REVISION_", revision)
        }
        val opt2 = for (o <- opt; sbtVersion <- sbtVersionOpt; scalaVersion <- scalaVersionOpt;
             revision <- revisionOpt; organisation <- organisationOpt) yield {
          val str1 = """ <property name="scalaVersion" value="""" + scalaVersion + """ />"""
          val str2 = """ <property name="sbtVersion" value="""" + sbtVersion + """ />"""
          val str3 = """ <property name="classifier" value="""" + revision + """ />"""
          val strs = for (s <- organisation.split("\\.")) yield
            """<property name="organisation1" value="""" + s + """" />"""
          o.replace("_REPLACESTR_", Array(str1, str2, str3, strs).mkString("\r\n"))
        }
        opt2.getOrElse("")
      }).forEach(s => writer.write(s))
      writer.close()*/
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