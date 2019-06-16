package jp.co.nri.nefs.tool.apllog

import java.io.{BufferedInputStream, FileInputStream, FileOutputStream}
import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}

import scala.util.control.Exception.allCatch

object ZipUtils {

  def main(args: Array[String]): Unit = {
    //ZipUtils.unzip("D:\\tmp\\zzz.zip")
    ZipUtils.zip("C:\\Users\\s2-nakamura\\Documents\\tmp\\a.txt")
  }

  def using[A <: java.io.Closeable](s: A)(f: A => Unit): Unit = {
    try { f(s) } finally { s.close() }
  }

  def zip(fileName: String): Unit = {
    val path = Paths.get(fileName)
    val index = fileName.lastIndexOf(".")
    val base = fileName.substring(0, index)
    val zipPath = Paths.get(base + ".zip")
    val ins = Files.newInputStream(path)
    val name = path.getFileName.toString
    val zos = new ZipOutputStream(Files.newOutputStream(zipPath), Charset.forName("Shift_JIS"))
    val entry = new ZipEntry(name)
    zos.putNextEntry(entry)
    val bis = new BufferedInputStream(ins)
    var buf = new Array[Byte](1024)
    var len = 0
    using(zos){zs =>
      Iterator.continually(bis.read(buf)).takeWhile(_ != -1).foreach(zs.write(buf, 0, _))
      zs.closeEntry()
    }
  }

  def unzip(zipPath: String): Unit = {
    val path = Paths.get(zipPath)
    val regex = """(^.+)\.((?i)zip)$""".r
    val fileName = path.getFileName.toString match {
      case regex(fileName, _) => fileName
      case _ => throw new java.lang.IllegalArgumentException("Not Zip file")
    }

    val zis = new ZipInputStream(Files.newInputStream(path))
    using(zis){zs =>
      Iterator.continually(zs.getNextEntry)
        .takeWhile(_ != null)
        .filterNot(_.isDirectory)
        .foreach(e => {
          val fos = Files.newOutputStream(path.getParent.resolve(e.getName))
          using(fos){fs => {
            Iterator.continually(zs.read()).takeWhile(_ != -1).foreach(fs write _)
            zs.closeEntry()
          }}
        })
    }
  }
}
