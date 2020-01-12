package jp.co.nri.nefs.tool.log.analysis

import jp.co.nri.nefs.tool.log.common.model.{Log, WindowDetail}
import org.scalatest._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

class GetWindowNameSpec extends FlatSpec with PrivateMethodTester {

  val logAnalyzer = new LogAnalyzer
  val getWindowName = PrivateMethod[String]('getWindowName)

  def fixture =
    new {
      val logs = List(
        "2019-12-16 12:06:38.278 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.SmartSplitHandler]",
        "2019-12-16 12:06:38.687 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.SmartSplitHandler]",
        "2019-12-16 12:06:38.708 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
        "2019-12-16 12:06:46.091 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
        "2019-12-16 12:06:46.380 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
        "2019-12-16 12:06:46.391 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:46.980 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:48.038 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:48.533 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:48.629 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:48.665 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]"
      )
    }

  "WindowName" should "be get from message within parentheses correctly." in  {
    val out1 = logAnalyzer invokePrivate getWindowName("[Select Symbol Multi]Dialog opened.","SelectMultiDialog")
    assert(out1 === "Select Symbol Multi")
    val out2 = logAnalyzer invokePrivate getWindowName("[Smart Split    - Parent Order]Dialog opened.","SmartSplitDialog")
    assert(out2 === "Smart Split    - Parent Order")
  }

  it should "be get from clazz when parentheses exists in the middle of message." in  {
    val out1 = logAnalyzer invokePrivate getWindowName("aaa[Select Symbol Multi]Dialog open.", "SelectMultiDialog")
    assert(out1 === "SelectMultiDialog")
  }

  it should "be get from clazz when parentheses in message dose not exist." in  {
    val out1 = logAnalyzer invokePrivate getWindowName("Opened.","QuestionDialog")
    assert(out1 === "QuestionDialog")
  }

}


trait HandlerLog {
  val log = List(
    "2019-12-16 12:06:38.278 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.SmartSplitHandler]",
    "2019-12-16 12:06:38.687 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.SmartSplitHandler]",
    "2019-12-16 12:06:38.708 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
    "2019-12-16 12:06:46.091 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
    "2019-12-16 12:06:46.380 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
    "2019-12-16 12:06:46.391 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:46.980 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:48.038 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:48.533 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:48.629 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:48.665 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]"
  )
}

