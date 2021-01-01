package jp.co.nri.nefs.tool.transport

import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.util.FileUtils

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.Properties

object Cache2Local extends LazyLogging{

  final val BUILD_FILE_DIR = "build-file-dir"
  final val IS_AND_EXECUTION = "is-ant-execution"

  import jp.co.nri.nefs.tool.util.config.RichConfig._

  private val config = ConfigFactory.load()
  private val buildFileDir = Paths.get(config.getString(BUILD_FILE_DIR, logger))
  private val isAntExecution = config.getBoolean(IS_AND_EXECUTION, logger)

  def main(args: Array[String]): Unit = {

    if (Files.exists(buildFileDir)) {
      logger.info(s"Deleting $buildFileDir")
      FileUtils.delete(buildFileDir)
      logger.info("Done.")
    }
    Files.createDirectories(buildFileDir)
    logger.info(s"Created $buildFileDir")

    import Ivys._

    for {
      pFile <- propertyFiles
      ivyFile <- ivyFileOption(pFile)
      artifact = Artifact.createArtifact(pFile, ivyFile)
    } {
      logger.info(s"${buildFileDir.resolve(artifact.buildFileName)}:")
      val s = Properties.lineSeparator + artifact.buildFileBuffer.mkString(Properties.lineSeparator)
      logger.info(s)
      val buildFile = buildFileDir.resolve(artifact.buildFileName)
      Files.write(buildFile, artifact.buildFileBuffer.asJava)
      if (isAntExecution)
        AntExecutor(buildFile).execute()
    }
  }

}

case class AntExecutor(path: Path) extends LazyLogging {
  import AntExecutor._
  def execute(): Unit = {
    val execResult = execute(Seq("cmd", "/c", antBinary, "-f", path.toString))
    if (execResult.result == 0) {
      logger.info(s"$path execution succeeded.")
    } else {
      logger.error(s"$path execution failed.")
    }
    execResult.out.foreach(s => logger.info(s))
    execResult.err.foreach(s => logger.error(s))

  }

  private def execute(cmd: Seq[String]): ExecResult = {
    import scala.sys.process._

    val out = ListBuffer[String]()
    val err = ListBuffer[String]()

    val processLogger = ProcessLogger(
      (o: String) => out += o,
      (e: String) => err += e
    )

    val r = Process(cmd) ! processLogger

    ExecResult(r, out.toList, err.toList)
  }
}

object AntExecutor extends LazyLogging {
  final val ANT_BINARY = "ant-binary"
  private val config = ConfigFactory.load()
  import jp.co.nri.nefs.tool.util.config.RichConfig._
  private val antBinary = config.getString(ANT_BINARY, logger)
}

case class ExecResult(result: Int, out: List[String], err: List[String])
