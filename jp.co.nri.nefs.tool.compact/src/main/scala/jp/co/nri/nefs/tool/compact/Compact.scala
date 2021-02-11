package jp.co.nri.nefs.tool.compact

import java.nio.file.{Files, Path}
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.util.FileUtils
import scala.collection.mutable.ListBuffer

object Compact extends LazyLogging {

  def main(args: Array[String]): Unit = {

    import scala.collection.JavaConverters._
    import Compacts._

    writeFile("build.sbt", Seq(base.resolve("build.sbt")))

    for {
      project <- projects
      files = FileUtils.autoClose(Files.walk(base)) { stream =>
        stream.iterator().asScala.filter { p =>
          p.toString.contains(project) && p.toString.contains("src\\main") && !Files.isDirectory(p)
        }.toList
      }
    } {
      writeFile(project, files)
    }

  }

  private def writeFile(fileName: String, files: Seq[Path]): Unit = {
    import scala.collection.JavaConverters._
    import Compacts._

    val buffer = ListBuffer[String]()
    for (file <- files) {
      val path = file.toString.replace(base.toString, "").drop(1)
      buffer += s"$begin $path"
      buffer ++= Files.readAllLines(file).asScala
      buffer += end
    }
    Files.write(fromOut.resolve(s"$fileName.txt"), buffer.asJava)

  }

}