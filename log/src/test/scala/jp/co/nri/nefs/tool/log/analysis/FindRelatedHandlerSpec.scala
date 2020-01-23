package jp.co.nri.nefs.tool.log.analysis

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FlatSpec

import scala.collection.JavaConverters._

class FindRelatedHandlerSpec extends FlatSpec with LogAnalysisService with
  TestingEnvironment with LogCollection {
  implicit val config: Config = ConfigFactory.load()
  val reader = new MockReader
  class MockReader extends Reader {
    def read: Map[String, java.util.stream.Stream[String]] = {
      Map(
        newSplitName -> newSplitLog.asJava.stream(),
        newSplitFromSmartName -> newSplitFromSmartLog.asJava.stream()
      )
    }
  }

  "Message" should "be analyzed according to LineInfo regular expression" in {
    val lineInfo = LineInfo.valueOf(newSplitLog.head)
    assert(lineInfo === Some(LineInfo("2019-10-10 15:54:12.452","OMS","INFO","TradeSheet","Handler start.","main","j.c.n.n.o.t.h.NewSplitHandler")))
  }

  it should "be analyzed even though message contains brackets" in {
    val lineInfo = LineInfo.valueOf(newSplitLog(2))
    assert(lineInfo === Some(LineInfo("2019-10-10 15:54:12.830","OMS","INFO","TradeSheet","[New Split    - Parent Order]Dialog opened.","main","j.c.n.n.o.r.p.d.s.n.NewSplitDialog")))
  }

  "Handler" should "be found" in  {
    analyze()
    windowBuffer
  }

  "Window" should "be bind with handler which is the nearest and is related with it" in {
    analyze()
    output.foreach(println)
  }

  "Handler" should "be bind with window" in {
    output.foreach(println)
    assert(output.head.activator === Some("SmartSplitHandler"))

    //case Some(window) => analysisWriter.write(window.toWindowDetail)
  }
}
