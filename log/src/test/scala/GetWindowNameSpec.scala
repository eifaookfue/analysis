import java.nio.file.Files

import jp.co.nri.nefs.tool.log.analysis.Log2Case
import org.scalatest._


class GetWindowNameSpec extends FlatSpec with PrivateMethodTester {

  def withLog2Case(testCode: Log2Case => Unit): Unit = {
    val path = Files.createTempFile("prefix","suffix")
    val log2Case = new Log2Case(path)
    try {
      testCode(log2Case)
    } finally {
      Files.delete(path)
    }
  }

  "WindowName" should "be get from message within parentheses correctly." in withLog2Case { log2Case =>
    val getWindowName = PrivateMethod[String]('getWindowName)
    val out1 = log2Case invokePrivate getWindowName("[Select Symbol Multi]Dialog opened.","SelectMultiDialog")
    assert(out1 === "Select Symbol Multi")
    val out2 = log2Case invokePrivate getWindowName("[Smart Split    - Parent Order]Dialog opened.","SmartSplitDialog")
    assert(out2 === "Smart Split    - Parent Order")
  }

  it should "be get from clazz when parentheses exists in the middle of message." in withLog2Case { log2Case =>
    val getWindowName = PrivateMethod[String]('getWindowName)
    val out1 = log2Case invokePrivate getWindowName("aaa[Select Symbol Multi]Dialog open.", "SelectMultiDialog")
    assert(out1 === "SelectMultiDialog")
  }

  it should "be get from clazz when parentheses in message dose not exist." in withLog2Case { log2Case: Log2Case =>
    val getWindowName = PrivateMethod[String]('getWindowName)
    val out1 = log2Case invokePrivate getWindowName("Opened.","QuestionDialog")
    assert(out1 === "QuestionDialog")
  }

}
