
package jp.co.nri.nefs.tool.transfer
import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}

import jp.co.nri.nefs.tool.log.common.utils.{FileUtils, ZipCommand, ZipUtils}
//import java.util.concurrent.Executors

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.{LazyLogging, Logger}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.implicitConversions

object ConfigKey {
  val DELETED_FILE_PATH = "deletedFilePath"
  val TRGFILE_BASE = "trgFileBase"
  val TRGFILE_NAME = "trgFileName"
  val TRGFILE_PATTERN = "trgFilePattern"
  val TRGFILE_DELIMITER = "trgFileDelimiter"
  val TRGFILE_LAD = "trgFileLAD"
  val TARGETFILE_BASE = "targetFileBase"
  val SLEEP_SECONDS = "sleepSeconds"
  val NUM_OF_THREADS = "numOfThreads"
  val MASKED_KEY_WORDS = "maskedKeyWords"
  val AWAIT_LIMIT_MINUTES = "awaitLimitMinutes"
  val ZIP_CMD = "zipCmd"
}

object RichConfig {
  implicit def configToRichConfig(config: Config): RichConfig = new RichConfig(config)
}

class RichConfig(config: Config) {
  def getString(s: String, logger: Logger): String = {
    val str = config.getString(s)
    logging(s, str, logger)
    str
  }
  def getInt(s: String, logger: Logger): Int = {
    val value = config.getInt(s)
    logging(s, value, logger)
    value
  }
  def getStringList(s: String, logger: Logger): List[String] = {
    val value = config.getStringList(s).asScala.toList
    logging(s, value, logger)
    value
  }

  private def logging(s: String, o: Any, logger: Logger): Unit = {
    logger.info("loaded {} = {}", s, o)
  }
}

object Transfer extends LazyLogging {

  import RichConfig._
  val config: Config = ConfigFactory.load()
  val LINE_SEPARATOR = ","
  val KEYVALUE_SEPARATOR = "="
  val format = new java.text.SimpleDateFormat("yyyyMMdd")
  val date = new java.util.Date()
  val today: String = format.format(date)
  val CHARSETNAME: Charset = Charset.forName("MS932")
  val TMPDIRNAME = "tmp"

  def main(args: Array[String]): Unit = {

    import ConfigKey._
    import FileUtils.autoClose

    // ファイルの削除
    val deletedFilePath = config.getString(DELETED_FILE_PATH)
    if (deletedFilePath != null){
      val path = Paths.get(deletedFilePath)
      delete(path)
    } else {
      logger.info("Skipped deleting")
    }

    // トリガファイルの作成
    val trgFileBase = config.getString(TRGFILE_BASE, logger)
    if (trgFileBase == null){
      logger.info("exit because {} isn't specified. ", TRGFILE_BASE)
      sys.exit()
    }

    val trgFileName = config.getString(TRGFILE_NAME, logger)
    val trgFileDelimiter = config.getString(TRGFILE_DELIMITER, logger)
    val trgFilePattern = config.getString(TRGFILE_PATTERN, logger)
    val trgFileLAD = config.getString(TRGFILE_LAD, logger)
    // W:\ASKA\OMS\OMS\USER\L1Support\Transfer
    val targetFileBase = Paths.get(config.getString(TARGETFILE_BASE, logger))
    // W:\ASKA\OMS\OMS\USER\L1Support\Transfer\tmp
    val targetFileTmp = targetFileBase.resolve(TMPDIRNAME)
    // W:\ASKA\OMS\OMS\USER\L1Support\Transfer\tmp\20191103
    val targetFileTmpToday = targetFileTmp.resolve(today)
    // W:\ASKA\OMS\OMS\USER\L1Support\Transfer\20191103
    val targetFileOut = targetFileBase.resolve(today)
    createTrgFile(trgFileBase, trgFileName, trgFileDelimiter, trgFilePattern, trgFileLAD, targetFileTmpToday)

    // 一定期間待機
    val sleepSeconds = config.getInt(SLEEP_SECONDS, logger)
    if (sleepSeconds > 0) {
      logger.info("sleeping {} seconds", sleepSeconds)
      Thread.sleep(sleepSeconds * 1000)
    }

    // ファイル一覧の取得
    val lists = autoClose(Files.walk(targetFileTmpToday)){
      stream =>
        stream.iterator().asScala.toList
    }

    // マスク化
    //implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(config.getInt(NUM_OF_THREADS, logger)))
    val maskedKeyWords = config.getStringList(MASKED_KEY_WORDS, logger)
    val awaitLimitMinutes = config.getInt(AWAIT_LIMIT_MINUTES, logger)
    maskAll(lists, maskedKeyWords, targetFileOut, awaitLimitMinutes)

    // 圧縮
    implicit val zipCmd: ZipCommand = new ZipCommand(config.getString(ZIP_CMD, logger))
    ZipUtils.zip(targetFileOut)

    // 削除
    Thread.sleep(3000)
    FileUtils.delete(targetFileTmp)
    FileUtils.delete(targetFileOut)
  }

  /*
  KEYVALUE_SEPARATORで分割してtrimしてKey,Valueを返す
   */
  private def keyValue(str: String): (Option[String], String) = {
    val tokens = str.split(KEYVALUE_SEPARATOR)
    if (tokens.length == 2){
      (Some(tokens(0).trim), tokens(1).trim)
    } else {
      (None, str)
    }
  }

  private def maskAll(lists: List[Path], maskedKeyWords: List[String], trgFileOut: Path,
                      awaitLimitMinutes: Int): Unit = {
    val tasks = for {
      file <- lists
      if file.toFile.isFile
    } yield Future {
      // autoCloseを用いるとjava.io.IOException: Stream closedがでてしまう
      // iteratorを返却した時点でcloseしてしまう
      val stream = Files.lines(file, CHARSETNAME)
      val lines = stream.iterator().asScala
      val outPath = resolveOutpath(trgFileOut, file)
      Files.write(outPath, lines.map(line =>
        mask(line, maskedKeyWords)).toIterable.asJava, CHARSETNAME)
      logger.info("output to {} completed.", outPath)
      stream.close()
      outPath
    }
    val aggregated: Future[List[Path]] = Future.sequence(tasks)
    Await.result(aggregated, awaitLimitMinutes.minutes)
  }

  private def mask(str: String, maskedKeyWords: List[String]): String = {
    (for {
      tokens <- str.split(LINE_SEPARATOR)
      (keyOpt, value) = keyValue(tokens)
      newValue = keyOpt.map{key =>
        if (maskedKeyWords.contains(key))
          key + KEYVALUE_SEPARATOR + "****"
        else
          key + KEYVALUE_SEPARATOR + value
      }
        .getOrElse(value)
    } yield newValue).reduceLeft(_ + LINE_SEPARATOR + _)
  }

  private def delete(path: Path): Unit = {
    val lines = Files.lines(path, CHARSETNAME).iterator().asScala
    for {
      line <- lines
      if  !line.startsWith("#")
      path = Paths.get(line)
    } {
      logger.info("Deleting {}", path)
      Files.delete(path)
    }
  }

  private def createTrgFile(trgFileBase: String, trgFileName: String, trgFileDelimiter: String,
                            trgFilePattern: String, trgFileLAD: String, targetFileTmpToday: Path): Unit = {
    Files.createDirectories(targetFileTmpToday)
    logger.info("created {}", targetFileTmpToday)
    val path = Paths.get(trgFileBase)
    val list = Files.list(path).iterator().asScala
    for {
      p <- list
      user = p.getFileName.toString
      outPath = p.resolve(trgFileName)
      // W:\ASKA\OMS\OMS\USER\L1Support\Transfer\tmp\20191103\356435
      target =  targetFileTmpToday.resolve(user).toString
      str = List(trgFilePattern + trgFileDelimiter + target + trgFileDelimiter + trgFileLAD)
    } {
      Files.write(outPath, str.asJava)
    }
  }
  /*
  targetOut: W:\ASKA\OMS\OMS\USER\L1Support\Transfer\20191103
  path: W:\ASKA\OMS\OMS\USER\L1Support\Transfer\tmp\20191103\356435\TradeSheet...log
  return: W:\ASKA\OMS\OMS\USER\L1Support\Transfer\20191103\356435\TradeSheet..._mask.log
   */
  private def resolveOutpath(targetOut: Path, path: Path): Path = {
    import jp.co.nri.nefs.tool.log.common.utils.RichFiles._
    val name = path.getFileName.toString
    val base = name.basename
    val ext = name.extension
    val newName = base + "_mask." + ext
    val parent = targetOut.resolve(path.getParent.getFileName.toFile.toString)
    Files.createDirectories(parent)
    parent.resolve(newName)
  }



}