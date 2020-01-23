package jp.co.nri.nefs.tool.log.analysis

import org.scalatest._

import scala.collection.JavaConverters._

class GetWindowNameSpec extends FlatSpec with PrivateMethodTester {

  val testObject: TestingEnvironment = new TestingEnvironment {
    val reader = new MockReader
    class MockReader extends Reader with LogCollection {
      def read: Map[String, java.util.stream.Stream[String]] = {
        Map(newSplitFromSmartName -> newSplitFromSmartLog.asJava.stream())
      }
    }

    //val lineInfo = LineInfo.valueOf()

  }

  val findRelatedHandler: PrivateMethod[Option[String]] = PrivateMethod[Option[String]]('findRelatedHandler)

  "Handler" should "be found." in {

  }


  val getWindowName: PrivateMethod[String] = PrivateMethod[String]('getWindowName)




  "WindowName" should "be get from message within parentheses correctly." in  {
    val out1 = testObject invokePrivate getWindowName("[Select Symbol Multi]Dialog opened.","SelectMultiDialog")
    assert(out1 === "Select Symbol Multi")
    val out2 = testObject invokePrivate getWindowName("[Smart Split    - Parent Order]Dialog opened.","SmartSplitDialog")
    assert(out2 === "Smart Split    - Parent Order")
  }

  it should "be get from clazz when parentheses exists in the middle of message." in  {
    val out1 = testObject invokePrivate getWindowName("aaa[Select Symbol Multi]Dialog open.", "SelectMultiDialog")
    assert(out1 === "SelectMultiDialog")
  }

  it should "be get from clazz when parentheses in message dose not exist." in  {
    val out1 = testObject invokePrivate getWindowName("Opened.","QuestionDialog")
    assert(out1 === "QuestionDialog")
  }

}



