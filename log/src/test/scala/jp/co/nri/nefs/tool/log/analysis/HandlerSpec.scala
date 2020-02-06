package jp.co.nri.nefs.tool.log.analysis

import org.scalatest.{FlatSpec, PrivateMethodTester}

class HandlerSpec extends FlatSpec with PrivateMethodTester with TestingEnvironment with LogCollection {
  val findRelatedHandler = PrivateMethod[Option[Handler]]('findRelatedHandler)

  "NewSplitDialog" should "be bound to NewSplitHandler" in {
    val lineInfo1 = LineInfo.valueOf(newSplitLog(0)).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    val lineInfo2 = LineInfo.valueOf(newSplitLog(2)).get
    val result = logAnalyzer invokePrivate findRelatedHandler(Option(handler), lineInfo2.underlyingClass)
    assert(result === Option(handler))
  }

  it should "NOT be bound to SmartSplitHandler" in {
    val lineInfo1 = LineInfo.valueOf(newSplitFromSmartLog(0)).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    val lineInfo2 = LineInfo.valueOf(newSplitFromSmartLog(5)).get
    val result = logAnalyzer invokePrivate findRelatedHandler(Option(handler), lineInfo2.underlyingClass)
    assert(result === None)
  }

  "SmartSplitDialog" should "be bound to SmartSplitHandler" in {
    val lineInfo1 = LineInfo.valueOf(newSplitFromSmartLog(0)).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    val lineInfo2 = LineInfo.valueOf(newSplitFromSmartLog(2)).get
    val result = logAnalyzer invokePrivate findRelatedHandler(Option(handler), lineInfo2.underlyingClass)
    assert(result === Option(handler))
  }

  "QuestionDialog" should "be bound to CompleteOrderHandler" in {
    val lineInfo1 = LineInfo.valueOf(completeOrderLog(0)).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    val lineInfo2 = LineInfo.valueOf(completeOrderLog(2)).get
    val result = logAnalyzer invokePrivate findRelatedHandler(Option(handler), lineInfo2.underlyingClass)
    assert(result === Option(handler))
  }


}