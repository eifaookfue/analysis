package jp.co.nri.nefs.tool.log.common.utils

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.scalalogging.LazyLogging

import scala.language.implicitConversions

object RichFiles {
  implicit def stringToRichString(str: String): RichString = new RichString(str)
  implicit def pathToRichPath(path: Path): RichPath = new RichPath(path)
}

class RichString(str: String) {
  def basename: String = {
    val index = str.lastIndexOf('.')
    if (index != -1) str.substring(0, index) else str
  }
  def extension: String = {
    val index = str.lastIndexOf('.')
    if (index != -1) str.substring(index+1) else ""
  }
  def newExtension(extension: String): String = {
    basename + "." + extension
  }
}

class RichPath(path: Path) {
  import RichFiles.stringToRichString

  def basename: Path = {
    val basename = path.getFileName.toString.basename
    path.getParent.resolve(basename)
  }
  def newExtension(extension: String): Path = {
    val basename = path.getFileName.toString.basename
    path.getParent.resolve(basename + "." + extension)
  }
}

object FileUtils extends LazyLogging {

  def using[A <: java.io.Closeable](s: A)(f: A => Unit): Unit = {
    try { f(s) } finally { s.close() }
  }

  def autoClose[A <: AutoCloseable,B](closeable: A)(fun: A ⇒ B): B = {
    try {
      fun(closeable)
    } finally {
      closeable.close()
    }
  }

  def copyDir(fromBase: Path, from: Path, toBase: Path): Unit = {
    class Visitor extends SimpleFileVisitor[Path] {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        //dir=D:\Apl\.ivy2\cache\org.scala-lang\scala-compiler\docs
        val target = toBase.resolve(fromBase.relativize(dir))

        //上位ディレクトリをコピーする
        //fromBase = Paths.get("D:\Apl\.ivy2\cache")
        //from = Paths.get("D:\Apl\.ivy2\cache\org.scala-lang\scala-compiler")
        //toBase = Paths.get("D:\tmp\20190715000000")
        //のときorg.scala-langをまずコピーする
        val relative = fromBase.relativize(from)
        val dirs = relative.toString.split("\\\\")
        var tmpFrom = fromBase
        var tmpTo = toBase
        // dirs = Array("org.scala-lang", "scala-compiler")
        // dirs.length -2 = 0
        for (i <- 0 to dirs.length - 2) {
          tmpFrom = tmpFrom.resolve(dirs(i))
          tmpTo = tmpTo.resolve(dirs(i))
          if (Files.notExists(tmpTo)){
            Files.copy(tmpFrom, tmpTo)
          }
        }

        logger.info(s"copying from $dir to $target...")
        try {
          Files.copy(dir, target, StandardCopyOption.COPY_ATTRIBUTES)
          logger.info("done.")
        } catch {
          case _ : Exception => logger.warn("failed.")
        }
        FileVisitResult.CONTINUE
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =  {
        val target = toBase.resolve(fromBase.relativize(file))
        logger.info(s"copying from $file to $target...")
        try {
          Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES)
          logger.info("done.")
        } catch {
          case _ : Exception => logger.warn("failed.")
        }

        FileVisitResult.CONTINUE
      }
    }
    Files.walkFileTree(from, new Visitor)
  }

  def delete(path: Path): Unit = {
    class Visitor extends SimpleFileVisitor[Path] {
      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }
    }
    Files.walkFileTree(path, new Visitor())
  }
}