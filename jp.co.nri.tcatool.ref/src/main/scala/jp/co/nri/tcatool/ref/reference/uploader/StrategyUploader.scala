package jp.co.nri.tcatool.ref.reference.uploader

import java.nio.charset.Charset
import java.nio.file.Paths
import java.sql.Timestamp

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.common.upload.{DBUtil, SimpleUploader}
import jp.co.nri.tcatool.ref.reference.model.{Strategy, StrategyComponent}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.util.Try

class StrategyUploader @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends StrategyComponent with SimpleUploader[Strategy] with DBUtil[Strategy]
  with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging {

  import profile.api._

  val query = TableQuery[Strategies]

  override def convert(row: List[String]): Try[Strategy] = {
    for {
      strategyName <- Read[String].reads(row.head)
      strategyType <- Read[String].reads(row(1))
      updateTime = new Timestamp(System.currentTimeMillis())
      strategy = Strategy(strategyName, strategyType, updateTime)
    } yield strategy
  }

}

object StrategyUploader extends LazyLogging {

  final val CONFIG_BASE = "StrategyUploader"
  final val INPUT_DIR = CONFIG_BASE + ".input-dir"
  final val INPUT_FILE = CONFIG_BASE + ".input-file"
  final val CHARSET_NAME = CONFIG_BASE + ".charset-name"
  final val SPLITTER = CONFIG_BASE + ".splitter"
  final val MAX_NUMBER_OF_LINE = CONFIG_BASE + ".max-number-of-line"
  final val START_NUMBER = CONFIG_BASE + ".start-number"
  final val UPLOAD_PER_COUNT = CONFIG_BASE + ".upload-per-count"

  import jp.co.nri.nefs.tool.util.config.RichConfig._

  private val config = ConfigFactory.load()
  private val input = Paths.get(config.getString(INPUT_DIR, logger)).resolve(config.getString(INPUT_FILE, logger))
  private val charSet = Charset.forName(config.getString(CHARSET_NAME, logger))
  private val uploadPerCount = config.getInt(UPLOAD_PER_COUNT, logger)
  private val maxNumberOfLine = Try(config.getInt(MAX_NUMBER_OF_LINE, logger)).map(Option(_)).getOrElse(None)
  private val startNumber = config.getInt(START_NUMBER, logger)
  private val splitter: String = config.getString(SPLITTER, logger)

  def main(args: Array[String]): Unit = {

    ServiceInjector.initialize()
    val strategyUploader = ServiceInjector.getComponent(classOf[StrategyUploader])
    strategyUploader.upload(input, splitter, charSet, uploadPerCount, startNumber, maxNumberOfLine)

  }

}
