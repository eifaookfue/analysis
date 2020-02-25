package jp.co.nri.nefs.tool.analytics.store.client.classify

import jp.co.nri.nefs.tool.analytics.store.client.{LogCollection, TestingEnvironment}
import org.scalatest.{FlatSpec, PrivateMethodTester}

import scala.collection.mutable.ListBuffer

class HandlerSpec extends FlatSpec with PrivateMethodTester with TestingEnvironment with LogCollection {
  private val findRelatedHandler = PrivateMethod[Option[Handler]]('findRelatedHandler)

  "NewSplitHandler" should "have handler name called NewSplitHandler" in {
    val lineInfo1 = LineInfo.valueOf(newSplitLog.head).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    assert(handler.name === "NewSplitHandler")
  }

  "NewSplitDialog" should "be bound to NewSplitHandler" in {
    val lineInfo1 = LineInfo.valueOf(newSplitLog.head).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    val handlerBuffer = ListBuffer(handler)
    val lineInfo2 = LineInfo.valueOf(newSplitLog(2)).get
    val result = clientLogCollector invokePrivate findRelatedHandler(handlerBuffer, lineInfo2.underlyingClass)
    assert(result === Option(handler))
  }

  it should "NOT be bound to SmartSplitHandler" in {
    val lineInfo1 = LineInfo.valueOf(newSplitFromSmartLog.head).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    val handlerBuffer = ListBuffer(handler)
    val lineInfo2 = LineInfo.valueOf(newSplitFromSmartLog(5)).get
    val result = clientLogCollector invokePrivate findRelatedHandler(handlerBuffer, lineInfo2.underlyingClass)
    assert(result === None)
  }

  "SmartSplitHandler" should "have handler name called SmartSplitHandler" in {
    val lineInfo1 = LineInfo.valueOf(newSplitFromSmartLog.head).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    assert(handler.name === "SmartSplitHandler")
  }

  "SmartSplitDialog" should "be bound to SmartSplitHandler" in {
    val lineInfo1 = LineInfo.valueOf(newSplitFromSmartLog.head).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    val handlerBuffer = ListBuffer(handler)
    val lineInfo2 = LineInfo.valueOf(newSplitFromSmartLog(2)).get
    val result = clientLogCollector invokePrivate findRelatedHandler(handlerBuffer, lineInfo2.underlyingClass)
    assert(result === Option(handler))
  }

  "QuestionDialog" should "be bound to CompleteOrderHandler" in {
    val lineInfo1 = LineInfo.valueOf(completeOrderLog.head).get
    val handler = Handler(lineInfo1.underlyingClass, LineTime(0, lineInfo1.datetime))
    val handlerBuffer = ListBuffer(handler)
    val lineInfo2 = LineInfo.valueOf(completeOrderLog(2)).get
    val result = clientLogCollector invokePrivate findRelatedHandler(handlerBuffer, lineInfo2.underlyingClass)
    assert(result === Option(handler))
  }


}