package jp.co.nri.tcatool.common.upload

import java.nio.charset.Charset
import java.nio.file.{Files, Path}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer
import scala.sys.process.{Process, ProcessLogger}
import scala.util.{Failure, Success, Try}

trait SimpleUploader[T] {

  self: DBUtil[T] with LazyLogging =>

  def upload(info: UploadInformation): Unit = upload(info.input, info.splitter, info.charSet, info.uploadPerCount,
    info.startNumber, info.maxNumberOfLine)

  def upload(input: Path, splitter: String, charSet: Charset,
             uploadPerCount: Int, startNumber: Int, maxNumberOfLine: Option[Int] = None): Unit = {

    val pConfig = ConfigFactory.load()
    val cConfig = pConfig.getConfig(getClass.getSimpleName)

    import jp.co.nri.nefs.tool.util.config.RichConfig._

    val isRecreate = Try(cConfig.getBoolean(SimpleUploader.IS_RECREATE, logger)).getOrElse(true)
    val isZip = Try(cConfig.getBoolean(SimpleUploader.IS_ZIP, logger)).getOrElse(false)
    val zipCmd = pConfig.getString(SimpleUploader.ZIP_CMD, logger)
    val unzipCmd = pConfig.getString(SimpleUploader.UNZIP_CMD, logger)

    if (isRecreate)
      recreateTable()

    import jp.co.nri.nefs.tool.util.RichFiles._

    val in = if (isZip) {
      exec(CmdExecutor(Seq(unzipCmd, input.toString)))
      input.getParent.resolve(input.getFileName.toString.basename)
    } else {
      input
    }

    import scala.collection.JavaConverters._

    val stream = Files.lines(in, charSet)
    val iterator = take(stream.iterator().asScala, maxNumberOfLine)
    try {
      val dataWithIndex = for {
        (line, index) <- iterator.zipWithIndex
        if index > startNumber - 1
        if !line.startsWith("+-") // will ignore separator of the bottom line.
        row = line.split(splitter).toList
        if condition(row)
        data = convert(row)
      } yield (data, index)
      val buffer = ListBuffer[T]()
      dataWithIndex.foreach {
        case (Success(v), index) =>
          buffer += v
          if ((index + 1) % uploadPerCount == 0) {
            logger.info(s"uploading $index...")
            upload(buffer)
            buffer.clear()
          }
        case (Failure(e), index) =>
          logger.error(s"$index: $e")
      }
      if (buffer.nonEmpty)
        upload(buffer)
    } finally {
      if (isZip) {
        exec(CmdExecutor(Seq(zipCmd, in.toString)))
      }
      stream.close()
    }

  }

  private def exec(executor: CmdExecutor): Unit = {
    logger.info(s"$executor started.")
    val execResult = executor.execute
    execResult.out.foreach(s => logger.info(s))
    execResult.err.foreach(s => logger.error(s))
    logger.info(s"$executor ended.")
    if (execResult.result != 0)
      throw new RuntimeException(s"$executor failed.")
  }


  def convert(row: List[String]): Try[T]

  def condition(row: List[String]): Boolean = true

  private def take[A](it: Iterator[A], maxNumberOfLine: Option[Int]): Iterator[A] = {
    maxNumberOfLine.map(it.take).getOrElse(it)
  }

}

case class ExecResult(result: Int, out: List[String], err: List[String])

case class CmdExecutor(cmd: Seq[String])  extends LazyLogging {
  val errorBufferPath: ListBuffer[Path] = ListBuffer[Path]()
  val errorBufferStr: ListBuffer[String] = ListBuffer[String]()

  def execute: ExecResult = {
    val out = ListBuffer[String]()
    val err = ListBuffer[String]()

    val pLogger = ProcessLogger(
      (o: String) => out += o,
      (e: String) => err += e
    )

    val r = Process(cmd) ! pLogger

    ExecResult(r, out.toList, err.toList)

  }

}


object SimpleUploader {
  final val IS_RECREATE = "is-recreate"
  final val IS_ZIP = "is-zip"
  final val ZIP_CMD = "zip-cmd"
  final val UNZIP_CMD = "unzip-cmd"
}
