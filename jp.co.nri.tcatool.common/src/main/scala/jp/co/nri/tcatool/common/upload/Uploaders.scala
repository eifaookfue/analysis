package jp.co.nri.tcatool.common.upload

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import com.typesafe.config.Config
import jp.co.nri.nefs.tool.util.FileUtils
import scala.collection.JavaConverters._

object Uploaders {

  final val INPUT_DIR = "input-dir"
  final val FILES = "files"
  final val INPUT_FILE = "input-file"
  final val CHARSET_NAME = "charset-name"
  final val SPLITTER = "splitter"
  final val MAX_NUMBER_OF_LINE = "max-number-of-line"
  final val START_NUMBER = "start-number"
  final val UPLOAD_PER_COUNT = "upload-per-count"
  final val SYMBOL = "symbol"
  final val BROKER = "broker"
  final val BROKER_ENTITY = "broker-entity"
  final val STRATEGY = "strategy"

  def uploadInformationSeq(config: Config): Seq[UploadInformation] = {
    createUploadAndMappingInformationSeq(config, shouldGetMapping = false).map(_._1.get)
  }

  def uploadAndMappingInformationSeq(config: Config): Seq[UploadAndMappingInformation] = {
    createUploadAndMappingInformationSeq(config, shouldGetMapping = true).map(_._2.get)
  }

  private def createUploadAndMappingInformationSeq(
                                                    config: Config,
                                                    shouldGetMapping: Boolean) : Seq[(Option[UploadInformation], Option[UploadAndMappingInformation])] = {
    val dir = Paths.get(config.getString(INPUT_DIR))
    val lists = createFileListMap(config)

    for {
      l <- lists
      regex = l(INPUT_FILE).asInstanceOf[String]
      splitter = l(SPLITTER).asInstanceOf[String]
      charset = Charset.forName(l(CHARSET_NAME).asInstanceOf[String])
      count = l(UPLOAD_PER_COUNT).asInstanceOf[Int]
      start = l(START_NUMBER).asInstanceOf[Int]
      max = l.get(MAX_NUMBER_OF_LINE).map(_.asInstanceOf[Int])
      files = findFile(dir, regex)
      f <- files
      uploadInformation = UploadInformation(f, splitter, charset, count, start, max)
      (uploadOp, uploadAndMapOp) = if (shouldGetMapping) {
        val symbolMapping = createMappingInfo(l, dir, SYMBOL)
        val brokerMapping = createMappingInfo(l, dir, BROKER)
        val brokerEntityMapping = createMappingInfo(l, dir, BROKER_ENTITY)
        val strategyMapping = createMappingInfo(l, dir, STRATEGY)
        (None,
          Some(
            UploadAndMappingInformation(uploadInformation, symbolMapping, brokerMapping,
              brokerEntityMapping, strategyMapping)
          )
        )
      } else {
        (Some(uploadInformation), None)
      }
    } yield (uploadOp, uploadAndMapOp)

  }

  private def findFile(dir: Path, regex: String): Seq[Path] = {
    FileUtils.autoClose(Files.list(dir)) { stream =>
      stream.iterator().asScala.filter(_.getFileName.toString.matches(regex)).toList.sorted // By dictionary order
    }
  }

  def createMappingInfo(configMap: Map[String, AnyRef], inputDir: Path, target: String): MappingInformation = {
    val targetMap = toMap(configMap(target))
    MappingInformation(
      inputDir.resolve(targetMap(INPUT_FILE).asInstanceOf[String]),
      targetMap(SPLITTER).asInstanceOf[String],
      Charset.forName(targetMap(CHARSET_NAME).asInstanceOf[String]),
      targetMap(START_NUMBER).asInstanceOf[Int]
    )
  }


  private def createFileListMap(config: Config): Seq[Map[String, AnyRef]]
  = config.getList(FILES).unwrapped().asScala.map(toMap)

  private def toMap(hashMap: AnyRef): Map[String, AnyRef] =
    hashMap.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap

}
