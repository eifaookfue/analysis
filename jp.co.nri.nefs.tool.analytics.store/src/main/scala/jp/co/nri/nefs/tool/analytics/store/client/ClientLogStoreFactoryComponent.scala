package jp.co.nri.nefs.tool.analytics.store.client

import java.io.ObjectOutputStream
import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.store.client.model.{Log, WindowDetail}
import jp.co.nri.nefs.tool.util.FileUtils

trait ClientLogStoreFactoryComponent {
  val clientLogStoreFactory: ClientLogStoreFactory

  trait ClientLogStoreFactory {
    def create(fileName: String): ClientLogStore
  }

  class DefaultClientLogStoreFactory extends ClientLogStoreFactory {
    private val config = ConfigFactory.load()
    private val outputDir: Path = Paths.get(config.getString(ConfigKey.OUT_DIR))
    def create(fileName: String): DefaultClientLogStore = {
      new DefaultClientLogStore(outputDir, fileName)
    }
  }

  trait ClientLogStore {
    def write(log: Log): Unit
    def write(detail: WindowDetail): Unit
  }

  class DefaultClientLogStore(outputDir: Path = null, fileName: String = null) extends ClientLogStore with LazyLogging {
    import jp.co.nri.nefs.tool.util.RichFiles._
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
      FileUtils.autoClose(stream){ s =>
        try {
          logger.info(obj.toString)
          s.writeObject(obj)
        } catch { case _: Exception => }
      }
    }

  }
}
