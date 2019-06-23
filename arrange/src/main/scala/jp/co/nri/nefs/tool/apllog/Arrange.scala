package jp.co.nri.nefs.tool.apllog

import java.nio.file.{Files, Paths}
import java.util.Date
import jp.co.nri.nefs.tool.utils.ZipUtils

object Arrange {
  def main(args: Array[String]): Unit = {
    val path = Paths.get("D:\\tmp3")
    val regex = """TradeSheet_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).log$""".r

    Files.list(path).filter(_.getFileName.toString.endsWith(".zip")).forEach{ orgZip =>
      val tmpDirName = "%tY%<tm%<td%<tH%<tM%<tS" format new Date()
      val tmpDir = path.resolve(tmpDirName)
      Files.createDirectories(tmpDir)
      val tmpZip = tmpDir.resolve(orgZip.getFileName)
      Files.copy(orgZip, tmpZip)
      ZipUtils.unzip(tmpZip)
      Files.delete(tmpZip)
      Files.list(tmpDir).forEach { expanded =>
        val regex(env, computer, userName, startTime) = expanded.getFileName.toString
        val tradeDate = startTime.take(8)
        val targetDir = path.resolve(tradeDate)
        Files.createDirectories(targetDir)
        val targetFile = targetDir.resolve(expanded.getFileName)
        if (Files.exists(targetFile)) {
          if (expanded.toFile.length > targetFile.toFile.length) {
            println(s"override from $expanded to $targetFile")
            Files.copy(expanded, targetFile)
          } else {
            println(s"skipped copy from $expanded to $targetFile because file already exists")
          }
        } else {
          println(s"copied from $expanded to $targetFile")
          Files.copy(expanded, targetFile)
        }
        Files.delete(expanded)
      }
      Files.delete(tmpDir)
    }
  }
}
