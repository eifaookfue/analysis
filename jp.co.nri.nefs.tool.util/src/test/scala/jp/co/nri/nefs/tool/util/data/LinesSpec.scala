package jp.co.nri.nefs.tool.util.data

import java.nio.file.Paths

import com.typesafe.config.ConfigFactory
import org.scalatest.FeatureSpec

class LinesSpec extends FeatureSpec{

  private val config = ConfigFactory.load()
  private val baseDir = Paths.get(config.getString(LinesSpec.BASE_DIR))
  private val bookName = config.getString(LinesSpec.BOOK_NAME)

  feature("generate method") {

    scenario("normal") {
      val path = baseDir.resolve(bookName)
      Lines.generate(path, "Sheet1", 0)

      Lines.generate(path, "日本語", 0)
    }

  }


}

object LinesSpec {
  final val BASE_CONFIG = "LinesSpec"
  final val BASE_DIR = BASE_CONFIG + ".base-dir"
  final val BOOK_NAME = BASE_CONFIG + ".book-name"
}