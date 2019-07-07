package jp.co.nri.nefs.tool.utils

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

object FileUtils {
  def copyDir(from: Path, to: Path): Unit = {
    class Visitor extends SimpleFileVisitor[Path] {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.copy(dir, to.resolve(from.relativize(dir)), StandardCopyOption.COPY_ATTRIBUTES)
        FileVisitResult.CONTINUE
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =  {
        Files.copy(file, to.resolve(from.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES)
        FileVisitResult.CONTINUE
      }
    }
    Files.walkFileTree(from, new Visitor)
  }
}