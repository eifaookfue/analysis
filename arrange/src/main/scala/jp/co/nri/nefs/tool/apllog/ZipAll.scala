package jp.co.nri.nefs.tool.apllog

import java.nio.file.{Files, Paths}
import jp.co.nri.nefs.tool.utils.ZipUtils

object ZipAll {
  // run "D:\\tmp3" 20190313
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("usage: start YYYYMMDD")
      sys.exit(-1)
    }
    val start = Paths.get(args(0))
    val regex = """(^\d{8}$)""".r

    val endDate = args(1) match {
      case regex(file) => file.toInt
      case _ => throw new java.lang.IllegalArgumentException("Invalid format. YYYYMMDD is required.")
    }

    Files.list(start).filter(p => regex.findFirstIn(p.getFileName.toString).nonEmpty)
      .forEach{ p =>
        if (p.getFileName.toString.toInt < endDate){
          Files.list(p).forEach{p2 =>
            print(s"zipping $p2...")
            ZipUtils.zip(p2)
            Files.delete(p2)
            println("done.")
          }
        }
      }

    /*Files.walk(start).filter(_.toFile.isFile).forEach { p =>
      if (p.getFileName.toString.toInt < endDate)
        return
      println(p)
    }*/
  }
}