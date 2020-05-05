package jp.co.nri.nefs.tool.analytics.store.client.classify

import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.store.client.{LogCollection, TestingEnvironment}
import org.scalatest.FlatSpec

class E9nSpec extends FlatSpec with TestingEnvironment with LogCollection with LazyLogging{
  "When there are one exception message followed by 5 reason messages and 10 stack trace messages and 1 other message, " +
    "13 messages" should "be stored at E9N_STACKTRACE" in {
    println("Input message[1]:")
    for ((log, index) <- Seq(Seq(slickE9nLog.head), slickE9nLog.slice(1,6), slickE9nLog.slice(7, slickE9nLog.size)).flatten.zipWithIndex) {
      println(s"$index: $log")
      clientLogClassifier.classify(log, index)
    }
    println("Output[1]:")
    e9nStackTraceOutput.foreach(println)
/*
    assert(e9nStackTraceOutput.head.lineNo === 1)
    assert(e9nStackTraceOutput.head.parentLinNo === None)
    assert(e9nStackTraceOutput.last.lineNo === 5)
    assert(e9nStackTraceOutput.last.parentLinNo === Some(1))
*/
    assert(e9nStackTraceOutput.size === 15)
    e9nStackTraceOutput.clear()
  }

  "When there are one exception message followed by 6 reason messages, " +
    "only 1 message" should "be stored at E9N_STACKTRACE" in {
    println("Input message[2]:")
    for ((log, index) <- slickE9nLog.zipWithIndex) {
      println(s"$index: $log")
      clientLogClassifier.classify(log, index)
    }
    println("Output[2]:")
    e9nStackTraceOutput.foreach(println)
    assert(e9nStackTraceOutput.size === 1)
    e9nStackTraceOutput.clear()
  }

}
