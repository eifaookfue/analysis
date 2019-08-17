package jp.co.nri.nefs.tool.log.file

import java.nio.file.{Files, Path, Paths}
import java.util.stream.Collectors
import jp.co.nri.nefs.tool.log.common.utils.FileUtils._
import jp.co.nri.nefs.tool.log.common.utils.ZipUtils._
import jp.co.nri.nefs.tool.log.common.utils.RegexUtils._
import scala.collection.JavaConverters._

class Processes(isZip: Boolean, searchPath: Path, outputPath: Path) {
  private def arrange(): Unit = {
    val zipPaths = Files.list(searchPath).collect(Collectors.toList()).asScala
    for (zipPath <- zipPaths
         if isZipFile(zipPath)
    ){
      // 解凍。フォルダが作成される
      val expandedPath = unzip(zipPath)
      val paths = Files.list(expandedPath).collect(Collectors.toList()).asScala
      for (path <- paths;
        omsAplInfo <- getOMSAplInfo(path.getFileName.toFile.toString)
      ){
        val tradeDate = omsAplInfo.tradeDate
        // 出力フォルダ[outputPath/日付]を作成
        val targetDir = outputPath.resolve(tradeDate)
        Files.createDirectories(targetDir)
        // 出力先ファイル
        val targetFile = targetDir.resolve(path.getFileName.toFile.toString)
        // 出力フォルダに出力しようとしたファイルの圧縮済みのファイルが存在したら展開する
        val targetZip = targetDir.resolve(replaceExtensionToZip(path.getFileName.toFile.toString))
        if (Files.exists(targetZip)) {
          print(s"$targetZip has found, so unzipping...")
          unzip(targetZip, isCreateDir = false)
          Files.delete(targetZip)
          println("done.")
        }
        // コピー元のほうがサイズが大きい場合は上書き、コピー先に存在しない場合はコピー、それ以外はスキップ
        if (Files.exists(targetFile)) {
          if (path.toFile.length > targetFile.toFile.length) {
            println(s"override from $path to $targetFile")
            Files.copy(path, targetFile)
          } else {
            println(s"skipped copy from $path to $targetFile because file already exists")
          }
        } else {
          println(s"copied from $path to $targetFile")
          Files.copy(path, targetFile)
        }
        if (isZip){
          print(s"zipping $targetFile...")
          zip(targetFile)
          println("done.")
          Files.delete(targetFile)
        }
      }
      // 解凍先のフォルダを丸ごと削除
      delete(expandedPath)
    }
    /*Files.list(searchPath).forEach(file => {
      if (file.toFile.isFile && file.getFileName.toString.endsWith(".zip")){
        val tmpDirName = "%tY%<tm%<td%<tH%<tM%<tS" format new Date()
        //val tmpDir = outputPath.resolve(tmpDirName)
        //Files.createDirectories(tmpDir)
        val tmpDir = Files.createTempDirectory(outputPath,"tmp")
        val tmpZip = tmpDir.resolve(file.getFileName)
        Files.copy(file, tmpZip)
        ZipUtils.unzip(tmpZip)
        Files.delete(tmpZip)
        Files.list(tmpDir).forEach { expanded =>
          val regex(env, computer, userName, startTime) = expanded.getFileName.toString
          val tradeDate = startTime.take(8)
          val targetDir = outputPath.resolve(tradeDate)
          Files.createDirectories(targetDir)
          val targetFile = targetDir.resolve(expanded.getFileName)
          val targetZip = getZipFile(targetFile)
          if (Files.exists(targetZip)){
            print(s"$targetZip has found, so unzipping...")
            ZipUtils.unzip(targetZip)
            println("done.")
          }
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
          if (isZip){
            print(s"zipping $targetFile...")
            ZipUtils.zip(targetFile)
            println("done.")
            Files.delete(targetFile)
          }

        }
        Files.delete(tmpDir)
      } else {
        println(s"$file was skipped because of non zip file.")
      }
    })*/

  }

}

object Processes {
  type OptionMap = Map[Symbol, String]
  val usage = """
        Usage: jp.co.nri.nefs.tool.log.file.Processes [--nozip] [--searchdir dir] [--outputdir dir]
        """
  def main(args: Array[String]): Unit = {
    //val defaultOptions = Map('isZip -> "TRUE", 'searchdir -> "D:\\tmp3", 'outputdir -> "D:\\tmp4")
    val defaultOptions = Map(Symbol("isZip") -> "TRUE")
    val options = nextOption(defaultOptions, args.toList)
    val (isZip, searchdir, outputdir) = getOption(options)

    val processes = new Processes(isZip, searchdir, outputdir)
    processes.arrange()
  }

  def getOption(options: OptionMap): (Boolean, Path, Path) = {
    val isZip = if (options(Symbol("isZip")) == "TRUE") true else false
    val searchdir = getPath(options, "searchdir")
    val outputdir = getPath(options, "outputdir")
    (isZip, searchdir, outputdir)
  }

  private def getPath(options: OptionMap, str: String): Path = {
    options.get(Symbol(str)) match {
      case Some(v) => Paths.get(v)
      case None =>
        println(usage)
        throw new IllegalArgumentException(s"$str is mandatory.")
    }
  }


  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    list match {
      case Nil => map
      case "--nozip" :: tail =>
        nextOption(map ++ Map(Symbol("isZip") -> "FALSE"), tail)
      case "--searchdir" :: value :: tail =>
        nextOption(map ++ Map('searchdir -> value), tail)
      case "--outputdir" :: value :: tail =>
        nextOption(map ++ Map('outputdir -> value), tail)
      case _ => println("Unknown option")
        println(usage)
        sys.exit(1)
    }
  }

}
