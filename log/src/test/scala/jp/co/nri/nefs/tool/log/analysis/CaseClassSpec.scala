package jp.co.nri.nefs.tool.log.analysis

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FlatSpec

class CaseClassSpec extends FlatSpec with TestingEnvironment with LogCollection {
  val config: Config = ConfigFactory.load()

  "LineInfo" should "be created according to regular expression" in {
    val lineInfo = LineInfo.valueOf(newSplitLog.head)
    assert(lineInfo === Some(LineInfo("2019-10-10 15:54:12.452","OMS","INFO","TradeSheet","Handler start.","main","j.c.n.n.o.t.h.NewSplitHandler")))
  }

  it should "be created even though message contains brackets" in {
    val lineInfo = LineInfo.valueOf(newSplitLog(2))
    assert(lineInfo === Some(LineInfo("2019-10-10 15:54:12.830","OMS","INFO","TradeSheet","[New Split    - Parent Order]Dialog opened.","main","j.c.n.n.o.r.p.d.s.n.NewSplitDialog")))
  }

  "Window name" should "be extracted from message in brackets" in {
    val lineInfo = LineInfo.valueOf(newSplitLog(2)).get
    assert(lineInfo.windowName === "New Split    - Parent Order")
  }

  it should "be simple class name if no brackets exist in message" in {
    val lineInfo = LineInfo.valueOf(completeOrderLog(2)).get
    assert(lineInfo.windowName === "QuestionDialog")
  }

  "Handler" should "be " in {
    for ((s, index) <- newSplitLog.zipWithIndex) {
      logAnalyzer.analyze(s, index)
    }
    output.foreach(println)

  }

  /*"Handler" should "be found" in  {
    analyze()
    windowBuffer
  }

  "Window" should "be bind with handler which is the nearest and is related with it" in {
    analyze()
    output.foreach(println)
  }

  "Handler" should "be bind with window" in {
    output.foreach(println)
    assert(output.head.activator === Some("SmartSplitHandler"))*/

    //case Some(window) => analysisWriter.write(window.toWindowDetail)

}
