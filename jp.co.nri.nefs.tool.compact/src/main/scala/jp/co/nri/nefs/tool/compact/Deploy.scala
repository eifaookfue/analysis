package jp.co.nri.nefs.tool.compact

import java.nio.file.{Files, Path}
import jp.co.nri.nefs.tool.util.FileUtils
import scala.collection.mutable.ListBuffer

object Deploy {

  def main(args: Array[String]): Unit = {
    import Compacts._
    import scala.collection.JavaConverters._

    val fromFiles = FileUtils.autoClose(Files.list(fromOut)) { stream =>
      stream.iterator().asScala.toList
    }

    for {
      file <- fromFiles
      files = ListBuffer[String]()
      startNumbers = ListBuffer[Int]()
      endNumbers = ListBuffer[Int]()
      (line, number) <- Files.readAllLines(file).asScala.zipWithIndex
      _ = line match {
        case s if s.startsWith(begin) =>
          files += s.replace(begin, "").drop(1)
          startNumbers += number + 1
        case s if s.startsWith(end) =>
          endNumbers += number
        case _ =>
      }
    } {
      writeFile(file, files, startNumbers, endNumbers)
    }
  }


  private def writeFile(orgFile: Path, files: Seq[String], startNumbers: Seq[Int], endNumbers: Seq[Int]): Unit = {
    import scala.collection.JavaConverters._
    import Compacts.toOut
    for {
      ((file, startNumber), endNumber) <- files zip startNumbers zip endNumbers
      lines = Files.readAllLines(orgFile).asScala
      slice = lines.slice(startNumber, endNumber)
    } {
      val out = toOut.resolve(file)
      Files.createDirectories(out.getParent)
      Files.write(out, slice.asJava)
    }
  }

}
