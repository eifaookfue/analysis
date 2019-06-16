package jp.co.nri.nefs.tool.apllog

import java.io.{BufferedInputStream, FileInputStream, FileOutputStream, File => JFile}
import java.util.zip.{ZipEntry, ZipFile, ZipInputStream}

import scala.util.control.Exception._
import java.nio.file.{Files, Paths}
import java.nio.charset.Charset
import java.util.zip.{ZipEntry, ZipOutputStream}

import collection.JavaConverters._



case class Zip(val path: JFile) {
  private val regex = """(^.+)\.((?i)zip)$""".r
  val name = path.getName match {
    case regex(name, _) => name
    case _ => throw new java.lang.IllegalArgumentException("Not Zip file")
  }

  def zip() = {
    val base = "D:\\tmp"
    val dir = Paths.get(base)
    Files.walk(dir).iterator().asScala.foreach( p => {
      val ins = Files.newInputStream(p)
      val name = p.getFileName.toString
      val zos = new ZipOutputStream(new FileOutputStream(name), Charset.forName("SJIS"))
      val entry = new ZipEntry(name)
      zos.putNextEntry(entry)
      val bis = new BufferedInputStream(ins)
      val readBytes = bis.read()
      if (readBytes != -1){
        zos.write(readBytes)
      }
      zos.closeEntry()
      zos.close()
    })
  }

  /*
    コンストラクタで指定されたZipファイル名でフォルダを作成し、そのフォルダにZipを展開する
   */
  def unzip(targetPath: JFile = path.getParentFile): Throwable Either JFile = {
    def using[A <: java.io.Closeable](s: A)(f: A => Unit): Unit = {
      try { f(s) } finally { s.close() }
    }

    val baseDirName = name
    val baseDirPath = new JFile(targetPath, baseDirName)
    baseDirPath.mkdir()

    allCatch either {
      val zis = new ZipInputStream(new FileInputStream(path))
      using(zis){zs =>
        Iterator.continually(zs.getNextEntry)
          .takeWhile(_ != null)
          .filterNot(_.isDirectory)
          .foreach(e => {
            val file = new JFile(baseDirPath, e.getName)
            file.getParentFile.mkdir()
            val fos = new FileOutputStream(file)

            using(fos){fs => {
              Iterator.continually(zs.read()).takeWhile(_ != -1).foreach(fs write _)
              zs.closeEntry()
            }}
          })
      }
      baseDirPath
    }
  }
}
