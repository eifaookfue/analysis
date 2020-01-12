package jp.co.nri.nefs.tool.log.analysis

import org.scalatest.FlatSpec
import scala.collection.JavaConverters._

class FindRelatedHandlerSpec extends FlatSpec with TestingEnvironment with ReaderComponent with HandlerLog {
  val reader = new MockReader
  val logAnalyzer = new LogAnalyzer
  class MockReader extends Reader {
    def read: Map[String, java.util.stream.Stream[String]] = {
      Map("TradeSheet_OMS_TKY_FID2CAD332_356435_20191216120638278.log" -> log.asJava.stream())
    }
  }

  "Handler" should "be bind with window" in {
    logAnalyzer.analyze()
    assert(output.head.activator === "SmartSplitHandler")

    //case Some(window) => analysisWriter.write(window.toWindowDetail)
  }
}
