package jp.co.nri.nefs.tool.analytics.store.client.classify

import jp.co.nri.nefs.tool.analytics.store.client.{LogCollection, TestingEnvironment}
import org.scalatest.{FlatSpec, PrivateMethodTester}

import scala.collection.mutable.ListBuffer

class WindowSpec extends FlatSpec with PrivateMethodTester with TestingEnvironment with LogCollection {
  private val findRelatedWindow = PrivateMethod[Option[Handler]]('findRelatedWindow)
  private val updateWithEnd = PrivateMethod[Unit]('updateWithEnd)

  "NewSplitDialog" should "be bound to SmartSplitDialog when it raised from SmartSplit" in {
    // SmartSplitDialogが格納されている状態
    val lineInfo1 = LineInfo.valueOf(newSplitFromSmartLog(2)).get
    val window = Window(lineInfo1.windowName, LineTime(0, lineInfo1.datetime), lineInfo1.underlyingClass)
    val windowBuffer = ListBuffer(window)

    // NewSplitDialogを検知
    val lineInfo2 = LineInfo.valueOf(newSplitFromSmartLog(5)).get
    val result = clientLogClassifier invokePrivate findRelatedWindow(windowBuffer, lineInfo2.underlyingClass)
    assert(result === Option(window))

  }

  "SelectMultiDialog" should "NOT be bound to anything" in {
    val windowBuffer = ListBuffer()
    // SelectMultiDialogを検知
    val lineInfo1 = LineInfo.valueOf(selectSymbolFromViewLog(2)).get
    val result = clientLogClassifier invokePrivate findRelatedWindow(windowBuffer, lineInfo1.underlyingClass)
    assert(result === None)
  }

  "NewSplitDialog" should "have end property in encounter of close message" in {

    // Dialog opened.
    val lineNo1 = 2
    val lineInfo1 = LineInfo.valueOf(newSplitLog(lineNo1)).get
    val window = Window(lineInfo1.windowName, LineTime(lineNo1, lineInfo1.datetime), lineInfo1.underlyingClass)
    val windowBuffer = ListBuffer(window)

    // Dialog closed.
    val lineNo2 = 6
    val lineInfo2 = LineInfo.valueOf(newSplitLog(lineNo2)).get
    val f: Function[Window, Window] = w => w.copy(end = Some(LineTime(lineNo2, lineInfo2.datetime)))
    clientLogClassifier invokePrivate updateWithEnd(lineInfo2.windowName, windowBuffer, "", lineNo2,f)
    assert(windowBuffer.head ===
      Window(lineInfo1.windowName, LineTime(lineNo1, lineInfo1.datetime), lineInfo1.underlyingClass, Some(LineTime(lineNo2, lineInfo2.datetime)))
    )

  }

  it should "have end property even if which raised in nesting" in {

    val lineNo1 = 4
    val lineInfo1 = LineInfo.valueOf(newSplitAndNewSplitLog(lineNo1)).get
    val window1 = Window(lineInfo1.windowName, LineTime(lineNo1, lineInfo1.datetime),lineInfo1.underlyingClass)
    val windowBuffer = ListBuffer(window1)

    val lineNo2 = 7
    val lineInfo2 = LineInfo.valueOf(newSplitAndNewSplitLog(lineNo2)).get
    val window2 = Window(lineInfo2.windowName, LineTime(lineNo2, lineInfo2.datetime),lineInfo2.underlyingClass)
    windowBuffer += window2

    val lineNo3 = 9
    val lineInfo3 = LineInfo.valueOf(newSplitAndNewSplitLog(lineNo3)).get
    val f3: Function[Window, Window] = w => w.copy(end = Some(LineTime(lineNo3, lineInfo3.datetime)))
    clientLogClassifier invokePrivate updateWithEnd(lineInfo3.windowName, windowBuffer, "", lineNo3,f3)

    val lineNo4 = 12
    val lineInfo4 = LineInfo.valueOf(newSplitAndNewSplitLog(lineNo4)).get
    val f4: Function[Window, Window] = w => w.copy(end = Some(LineTime(lineNo4, lineInfo4.datetime)))
    clientLogClassifier invokePrivate updateWithEnd(lineInfo4.windowName, windowBuffer, "", lineNo4,f4)

    assert(windowBuffer.head ===
      Window(lineInfo1.windowName, LineTime(lineNo1, lineInfo1.datetime), lineInfo1.underlyingClass, Some(LineTime(lineNo4, lineInfo4.datetime)))
    )

    assert(windowBuffer(1) ===
      Window(lineInfo2.windowName, LineTime(lineNo2, lineInfo2.datetime), lineInfo2.underlyingClass, Some(LineTime(lineNo3, lineInfo3.datetime)))
    )
  }

}