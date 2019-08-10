package jp.co.nri.nefs.tool.log.common.utils

import java.io.BufferedInputStream
import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}

object ZipUtils {

  def using[A <: java.io.Closeable](s: A)(f: A => Unit): Unit = {
    try { f(s) } finally { s.close() }
  }

  def zip(path: Path): Unit = {
    val fileName = path.getFileName.toString
    val index = fileName.lastIndexOf(".")
    val base = fileName.substring(0, index)
    val zipPath = path.getParent.resolve(base + ".zip")
    val ins = Files.newInputStream(path)
    val name = path.getFileName.toString
    val zos = new ZipOutputStream(Files.newOutputStream(zipPath), Charset.forName("Shift_JIS"))
    val entry = new ZipEntry(name)
    zos.putNextEntry(entry)
    val bis = new BufferedInputStream(ins)
    var buf = new Array[Byte](1024)
    var len = 0
    using(bis){bs =>
      using(zos){zs =>
        Iterator.continually(bs.read(buf)).takeWhile(_ != -1).foreach(zs.write(buf, 0, _))
        zs.closeEntry()
      }
    }
  }

  def zip(fileName: String): Unit = {
    val path = Paths.get(fileName)
    zip(path)
  }

  def unzip(path: Path): Unit = {
    val regex = """(^.+)\.((?i)zip)$""".r
    val fileName = path.getFileName.toString match {
      case regex(fileName, _) => fileName
      case _ => throw new java.lang.IllegalArgumentException("Not Zip file")
    }

    val zis = new ZipInputStream(Files.newInputStream(path), Charset.forName("Shift_JIS"))
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

  def unzip(zipPath: String): Unit = {
    val path = Paths.get(zipPath)
    unzip(path)
  }

  def main(args: Array[String]): Unit = {
    val path = Paths.get("D:\\tmp5")
    val tmpDir = Files.createTempDirectory(path, "tmp")
    val source = Paths.get("D:\\tmp3\\20190318.zip")
    val target = tmpDir.resolve("20190318.zip")
    Files.copy(source, target)
    Files.list(tmpDir).forEach(p => {
      //Files.delete(p)
      println(p)
    })
    Files.delete(target)
    Files.delete(tmpDir)
    val tmpDir2 = Files.createTempDirectory(path, "tmp")
    Thread.sleep(5000)
    Files.delete(tmpDir2)
  }
}
