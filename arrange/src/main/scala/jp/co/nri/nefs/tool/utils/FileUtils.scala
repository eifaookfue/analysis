package jp.co.nri.nefs.tool.utils

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

object FileUtils {
  /** +
    *
    * @param from
    * @param to
    */
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

        print(s"copying from $dir to $target...")
        Files.copy(dir, target, StandardCopyOption.COPY_ATTRIBUTES)
        println("done.")
        FileVisitResult.CONTINUE
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =  {
        val target = toBase.resolve(fromBase.relativize(file))
        print(s"copying from $file to $target...")
        Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES)
        println("done.")
        FileVisitResult.CONTINUE
      }
    }
    Files.walkFileTree(from, new Visitor)
  }
}