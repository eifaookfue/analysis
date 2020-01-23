package jp.co.nri.nefs.tool.log.analysis

import jp.co.nri.nefs.tool.log.common.model.{Log, WindowDetail}

import scala.collection.mutable.ListBuffer

trait TestingEnvironment extends LogAnalysisService with ReaderComponent with AnalysisWriterComponent {

  val analysisWriterFactory = new MockWriterFactory
  val output: ListBuffer[WindowDetail] = ListBuffer[WindowDetail]()

  class MockWriterFactory extends AnalysisWriterFactory {
    def create(fileName: String): MockWriter = {
      new MockWriter
    }
  }

  class MockWriter extends AnalysisWriter {
    def write(log: Log): Unit = {}

    def write(detail: WindowDetail): Unit = {
      output += detail
    }
  }
}