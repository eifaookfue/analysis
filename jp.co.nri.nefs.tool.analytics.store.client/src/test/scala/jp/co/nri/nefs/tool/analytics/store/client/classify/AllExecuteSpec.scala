package jp.co.nri.nefs.tool.analytics.store.client.classify

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import jp.co.nri.nefs.tool.analytics.store.client.TestingEnvironment
import jp.co.nri.nefs.tool.util.FileUtils
import org.scalatest.FlatSpec

import scala.collection.JavaConverters._

class AllExecuteSpec extends FlatSpec with TestingEnvironment {
  final val INPUT_DIR = "inputDir"
  val CHARSETNAME: Charset = Charset.forName("MS932")
  private val config = ConfigFactory.load()

  private val input = Paths.get(config.getString(INPUT_DIR))
  val files: List[Path] = FileUtils.autoClose(Files.list(input)) { s => s.iterator().asScala.toList }
  for (file <- files) {
    val stream = Files.lines(file, CHARSETNAME)
    for ((line, tmpNo) <- stream.iterator().asScala.zipWithIndex) {
      val lineNo = tmpNo + 1
      clientLogClassifier.classify(line, lineNo)
    }
    output.foreach(println)
    stream.close()
  }

}
