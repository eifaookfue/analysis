package jp.co.nri.nefs.tool.log.analysis

import java.io.ObjectOutputStream
import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.log.common.model.{Log, WindowDetail}
import jp.co.nri.nefs.tool.log.common.utils.FileUtils.autoClose

trait AnalysisWriterComponent {
  val analysisWriterFactory: AnalysisWriterFactory

  trait AnalysisWriterFactory {
    def create(fileName: String): AnalysisWriter
  }

  class DefaultAnalysisWriterFactory extends AnalysisWriterFactory {
    private val config = ConfigFactory.load()
    private val outputDir: Path = Paths.get(config.getString(ConfigKey.OUT_DIR))
    def create(fileName: String): DefaultAnalysisWriter = {
      new DefaultAnalysisWriter(outputDir, fileName)
    }
  }

  trait AnalysisWriter {
    def write(log: Log): Unit
    def write(detail: WindowDetail): Unit
  }

  class DefaultAnalysisWriter(outputDir: Path = null, fileName: String = null) extends AnalysisWriter with LazyLogging {
    import jp.co.nri.nefs.tool.log.common.utils.RichFiles.stringToRichString

    Files.createDirectories(outputDir)
    private lazy val outLogPath = outputDir.resolve(fileName.basename + Keywords.LOG_SUFFIX + Keywords.OBJ_EXTENSION)
    private lazy val logOutputStream = new ObjectOutputStream(Files.newOutputStream(outLogPath))
    private lazy val outDetailPath = outputDir.resolve(fileName.basename + Keywords.WINDOW_DETAIL_SUFFIX + Keywords.OBJ_EXTENSION)
    private lazy val detailOutputStream = new ObjectOutputStream(Files.newOutputStream(outDetailPath))

    def write(log: Log): Unit = {
      doWrite(log, logOutputStream)
    }

    def write(detail: WindowDetail): Unit = {
      doWrite(detail, detailOutputStream)
    }

    private def doWrite[T](obj: T, stream: ObjectOutputStream): Unit = {
      autoClose(stream){ s =>
        try {
          logger.info(obj.toString)
          s.writeObject(obj)
        } catch { case _: Exception => }
      }
    }

  }
}
