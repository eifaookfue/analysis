package jp.co.nri.nefs.tool.util

import java.io.BufferedInputStream
import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}

import com.typesafe.scalalogging.LazyLogging

import scala.language.implicitConversions
import scala.sys.process.{Process, ProcessLogger}


class ZipCommand(val cmd: String)

object ZipUtils extends LazyLogging{

  import FileUtils.using
  import RichFiles._

  def zip(path: Path)(implicit zipCmd: ZipCommand = null): Unit = {
    val fileName = path.getFileName.toString
    val zipPath = path.newExtension("zip")
    if (zipCmd != null){
      val processLogger = ProcessLogger(
        (o: String) => logger.debug(o),
        (e: String) => logger.warn(e)
      )
      val cmd =Seq(zipCmd.cmd, "cvfM", zipPath.toString, "-C", path.getParent.toString, fileName)
      Process(cmd) !processLogger
      return
    }

    val ins = Files.newInputStream(path)
    val name = path.getFileName.toString
    val zos = new ZipOutputStream(Files.newOutputStream(zipPath), Charset.forName("Shift_JIS"))
    val entry = new ZipEntry(name)
    zos.putNextEntry(entry)
    val bis = new BufferedInputStream(ins)
    val buf = new Array[Byte](1024)
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

  /*
     指定されたzipファイルのbasenameでフォルダを作成し、そこに展開します。
     @param zipファイル
     @param フォルダを作成しない場合はfalse
     @return 指定されたzipファイルのbasenameフォルダ
     @throws IllegalArgumentException zip拡張子がないファイルが指定されたとき
   */
  def unzip(path: Path, isCreateDir: Boolean = true): Path = {
    if (!isZipFile(path)) {
      throw new java.lang.IllegalArgumentException("Not Zip file")
    }
    val outPath = if (isCreateDir) path.basename else path.getParent
    if (isCreateDir) Files.createDirectories(outPath)

    val zis = new ZipInputStream(Files.newInputStream(path), Charset.forName("Shift_JIS"))
    using(zis){zs =>
      Iterator.continually(zs.getNextEntry)
        .takeWhile(_ != null)
        .filterNot(_.isDirectory)
        .foreach(e => {
          val fos = Files.newOutputStream(outPath.resolve(e.getName))
          using(fos){fs => {
            Iterator.continually(zs.read()).takeWhile(_ != -1).foreach(v => fs write v)
            zs.closeEntry()
          }}
        })
    }
    outPath
  }

  def unzip(zipPath: String): String = {
    val path = Paths.get(zipPath)
    val outPath = unzip(path)
    outPath.toFile.toString
  }

  def isZipFile(path: Path): Boolean = {
    lazy val regex = """(^.+)\.((?i)zip)$""".r
    path.getFileName.toFile.toString match {
      case regex(_,_) => true
      case _ => false
    }
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
