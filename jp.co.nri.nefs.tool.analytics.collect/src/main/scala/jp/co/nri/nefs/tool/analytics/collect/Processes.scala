package jp.co.nri.nefs.tool.analytics.collect

import java.nio.file.{Files, Path, Paths}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.{LazyLogging, Logger}
import jp.co.nri.nefs.tool.analytics.model.OMSAplInfo
import jp.co.nri.nefs.tool.util.{FileUtils, ZipCommand, ZipUtils}
import scala.language.implicitConversions
import scala.collection.JavaConverters._

object RichConfig {
  implicit def configToRichConfig(config: Config): RichConfig = new RichConfig(config)
}

class RichConfig(config: Config) {
  def getString(s: String, logger: Logger): String = {
    val str = config.getString(s)
    logging(s, str, logger)
    str
  }
  private def logging(s: String, o: Any, logger: Logger): Unit = {
    logger.info(s"loaded $s = $o")
  }
}

class Processes(isZip: Boolean, searchPath: Path, outputPath: Path) extends LazyLogging{
  private def arrange(): Unit = {
    import jp.co.nri.nefs.tool.util.RichFiles._
    import RichConfig.configToRichConfig

    val config = ConfigFactory.load()
    implicit val zipCmd: ZipCommand = new ZipCommand(config.getString("zipCmd", logger))

    val zipPaths = FileUtils.autoClose(Files.list(searchPath)) {
      stream => stream.iterator().asScala.toList
    }
    for (zipPath <- zipPaths
         if ZipUtils.isZipFile(zipPath)
    ){
      // 解凍。フォルダが作成される
      val expandedPath = ZipUtils.unzip(zipPath)
     /*
         zipファイルがファイル名のディレクトリを含んでいるものと含んでいないもの両方ある
         ディレクトリを含んでいるとき、同じディレクトリが二重で作成され、
         Files.listだと直下のディレクトリしかリストアップされない。
         そのため、Files.walkを利用。
      */
      val paths = FileUtils.autoClose(Files.walk(expandedPath)) {
        stream => stream.iterator().asScala.toList
      }
      for (path <- paths;
        omsAplInfo <- OMSAplInfo.valueOf(path.getFileName.toFile.toString)
      ){
        val tradeDate = omsAplInfo.tradeDate
        // 出力フォルダ[outputPath/日付]を作成
        val targetDir = outputPath.resolve(tradeDate)
        Files.createDirectories(targetDir)
        // 出力先ファイル
        val targetFile = targetDir.resolve(path.getFileName.toFile.toString)
        // 出力フォルダに出力しようとしたファイルの圧縮済みのファイルが存在したら展開する
        val targetZip = targetDir.resolve(path.getFileName.toString.newExtension("zip"))
        if (Files.exists(targetZip)) {
          print(s"$targetZip has found, so unzipping...")
          ZipUtils.unzip(targetZip, isCreateDir = false)
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
          ZipUtils.zip(targetFile)
          println("done.")
          Files.delete(targetFile)
        }
      }
      // 解凍先のフォルダを丸ごと削除
      FileUtils.delete(expandedPath)
    }
  }

}

object Processes {
  type OptionMap = Map[Symbol, String]
  val usage = """
        Usage: jp.co.nri.nefs.tool.analytics.collect.Processes [--nozip] [--searchdir dir] [--outputdir dir]
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
