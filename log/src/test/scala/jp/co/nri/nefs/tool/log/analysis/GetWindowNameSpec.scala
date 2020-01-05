package jp.co.nri.nefs.tool.log.analysis

import java.nio.file.Files

import jp.co.nri.nefs.tool.log.common.model.{Log, WindowDetail}
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

class FindRelatedHandlerSpec extends FlatSpec with PrivateMethodTester {

  def withLog2Case(testCode: Log2Case => Unit): Unit = {
    val path = Files.createTempFile("prefix","suffix")
    val log2Case = new Log2Case(path)
    try {
      testCode(log2Case)
    } finally {
      Files.delete(path)
    }
  }

  /*
    private def findRelatedHandler(handlerOp: Option[Handler], underlyingClass: String)
                            (implicit config: Config): Option[Handler] = {
    try {
      val values = config.getStringList("HandlerMapping" + "." + underlyingClass).asScala
      handlerOp.flatMap { handler =>
        if (values.contains(handler.name)) handlerOp else None
      }
    } catch { case _ :ConfigException.Missing => None }
  }
  class AnalysisReporterFactory {
    private var outputDir: Path = _
    def setOutputDir(outputDir: Path): Unit = {
      this.outputDir = outputDir
    }
    def create(fileName: String): AnalysisReporter = {
      new AnalysisReporter(outputDir, fileName)
    }
  }

  class AnalysisReporter(outputDir: Path, fileName: String) extends LazyLogging {

    import jp.co.nri.nefs.tool.log.common.utils.RichFiles.stringToRichString

    private lazy val outLogPath = outputDir.resolve(fileName.basename + "Log" + Keywords.OBJ_EXTENSION)
    private lazy val logOutputStream = new ObjectOutputStream(Files.newOutputStream(outLogPath))
    private lazy val outDetailPath = outputDir.resolve(fileName.basename + "Detail" + Keywords.OBJ_EXTENSION)
    private lazy val detailOutputStream = new ObjectOutputStream(Files.newOutputStream(outDetailPath))

    def report(log: Log): Unit = {
      doReport(log, logOutputStream)
    }

    def report(detail: WindowDetail): Unit = {
      doReport(detail, detailOutputStream)
    }

   */

}

trait TestingEnvironment extends AnalysisReporterComponent {
  val analysisReporterFactory = new MockFactory

  class MockFactory extends AnalysisReporterFactory {
    override def create(fileName: String): MockReporter = {
      new MockReporter
    }
  }

  class MockReporter extends AnalysisReporter {
    override def report(log: Log): Unit = super.report(log)

    override def report(detail: WindowDetail): Unit = super.report(detail)
  }
}