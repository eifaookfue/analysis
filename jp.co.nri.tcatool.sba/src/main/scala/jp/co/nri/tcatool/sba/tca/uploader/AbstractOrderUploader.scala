package jp.co.nri.tcatool.sba.tca.uploader

import java.nio.file.Files
import java.sql.Timestamp

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.common.upload._
import jp.co.nri.tcatool.sba.model.EHistoryType
import jp.co.nri.tcatool.sba.tca.model._
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.util.Try

trait AbstractOrderUploader[T] extends SimpleUploader[T] with DBUtil[T]
  with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging{

  final val KEY_VALUE_EXPRESSION = """(.*)=(.*)""".r
  final val STRATEGY = "Strategy"
  final val CONFIG_BASE = "OrderUploader"

  var symbolMap: Map[String, String] = _
  var brokerMap: Map[String, String] = _
  var brokerEntityMap: Map[String, String] = _
  var strategyMap: Map[(String, String), String] = _

  def createMapping(uploadAndMappingInformation: UploadAndMappingInformation): Unit = {
    symbolMap = new SymbolMappingCreator(uploadAndMappingInformation.symbol).create
    brokerMap = new TwoColumnMappingCreator(uploadAndMappingInformation.broker).create
    brokerEntityMap = new TwoColumnMappingCreator(uploadAndMappingInformation.brokerEntity).create
    strategyMap = new StrategyMappingCreator(uploadAndMappingInformation.strategy).create
  }

  private val config = ConfigFactory.load()
  val uploadAndMappingInformationSeq: Seq[UploadAndMappingInformation] =
    Uploaders.uploadAndMappingInformationSeq(config.getConfig(CONFIG_BASE))

  import jp.co.nri.tcatool.sba.tca.read.SBARead._

  def toCompId(row: List[String]): Try[String] = Read[String].reads(row(1))

  def toOrderId(row: List[String]): Try[String] = Read[String].reads(row(3))

  def toOrderKey(row: List[String]): Try[OrderKey] = {
    for {
      compId <- toCompId(row)
      orderId <- toOrderId(row)
    } yield OrderKey(compId, orderId)
  }

  def toHistoryType(row: List[String]): Try[EHistoryType] = Read[EHistoryType].reads(row(16))

  def toPrice(row: List[String]): Try[Option[BigDecimal]] = Read[BigDecimal].optionalReads(row(33))

  def toSliceQty(row: List[String]): Try[Int] = Read[BigDecimal].reads(row(34)).map(_.toInt)

  def toBrokerCode(row: List[String]): Try[String] = Read[String].reads(row(37))

  private val cumulativeValueZero = CumulativeValue(0, 0)

  val cumulatives: Cumulatives = Cumulatives(cumulativeValueZero, cumulativeValueZero, cumulativeValueZero,
      cumulativeValueZero, cumulativeValueZero, cumulativeValueZero)

  def toSliceTime(row: List[String]): Try[Timestamp] = Read[Timestamp].reads(row(43))

  def toNote(row: List[String]): Try[Note] = {
    for {
      externalNote <- Read[String].optionalReads(row(46))
      internalNote <- Read[String].optionalReads(row(47))
    } yield Note(externalNote, internalNote)
  }

  val (avgPrice, closedTime) = (None, None)

  val slippageNone = Slippage(None, None)

  val benchMark = BenchMark(slippageNone, slippageNone, slippageNone, slippageNone)

  def toStrategyName(
                      row: List[String],
                      brokerCode: String): Try[String] = {
    for {
      strategyCode <- toStrategyCode(row)
      brokerEntityCode <- Try(brokerEntityMap(brokerCode))
      strategyName <- Try(strategyMap((brokerEntityCode, strategyCode)))
    } yield strategyName
  }

  private def toStrategyCode(row: List[String]): Try[String] = {
    for {
      params <- Read[String].reads(row(74))
      parameters <- createParameters(params)
      strategyCode <- Try(parameters(STRATEGY))
    } yield strategyCode
  }

  private def createParameters(str: String): Try[Map[String, String]] = Try {
    val strategyArray = str.split(";")
    strategyArray.map {
      case KEY_VALUE_EXPRESSION(key, value) => (key.trim, value.trim)
      case _ => throw new IllegalArgumentException("Invalid strategy")
    }.toMap
  }


}


abstract class MappingCreator[K, V](info: MappingInformation) extends LazyLogging {

  def create: Map[K, V] = {
    logger.info(s"$getClass::create started.")
    val stream = Files.lines(info.input, info.charSet)
    val iterator = stream.iterator().asScala
    try {
      (for {
        (line, index) <- iterator.zipWithIndex
        if index > info.startNumber - 1
        if !line.startsWith("+-") // will ignore separator of the bottom line.
        if line.trim.nonEmpty
        row = line.split(info.splitter).toList
        (key, value) = convert(row).get // Will throw exception in failure
      } yield key -> value).toMap
    } catch {
      case e: Exception =>
        logger.error(s"$getClass::create failed.")
        throw e
    } finally {
      logger.info(s"$getClass::create ended.")
      stream.close()
    }
  }

  def convert(row: List[String]): Try[(K, V)]
}

class SymbolMappingCreator(info: MappingInformation) extends MappingCreator[String, String](info) {
  override def convert(row: List[String]): Try[(String, String)] = {
    for {
      nriCode <- Read[String].reads(row(2))
      siccCode <- Read[String].reads(row(5))
    } yield nriCode -> siccCode
  }
}

class TwoColumnMappingCreator(info: MappingInformation) extends MappingCreator[String, String](info) {
  override def convert(row: List[String]): Try[(String, String)] = {
    for {
      key <- Read[String].reads(row.head)
      value <- Read[String].reads(row(1))
    } yield key -> value
  }
}

class StrategyMappingCreator(info: MappingInformation) extends MappingCreator[(String, String), String](info) {
  override def convert(row: List[String]): Try[((String, String), String)] = {
    for {
      brokerEntityCode <- Read[String].reads(row(1))
      strategyCode <- Read[String].reads(row(2))
      strategyName <- Read[String].reads(row(4))
    } yield (brokerEntityCode, strategyCode) -> strategyName
  }
}
