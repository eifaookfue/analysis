package jp.co.nri.nefs.tool.analytics.store.client.classify
import jp.co.nri.nefs.tool.analytics.store.client.{H2Environment, LogCollection}
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import org.scalatest.FlatSpec

class E9nH2Spec extends FlatSpec with H2Environment with LogCollection {

  val dbSetup: DBSetup = ServiceInjector.getComponent(classOf[DBSetup])

  "If e9n already exists, E9N's count" should "be increment" in {
    dbSetup.initialize()
    println("Input message[1]:")
    for ((log, index) <- Seq(slickE9nLog.slice(0,4), slickE9nLog.slice(7, slickE9nLog.length)).flatten.zipWithIndex) {
      println(s"$index: $log")
      clientLogClassifier.classify(log, index)
    }
    println("Output message[1]:")
    dbSetup.e9nSeq.foreach(println)
    dbSetup.e9nStackTraceSeq.foreach(println)
    dbSetup.e9nDetailSeq.foreach(println)
    //assert(dbSetup.e9nSeq.head.count === 2)
  }

  "If e9n's length is different, E9N record" should "be inserted" in {
    dbSetup.initialize()
    println("Input message[2]:")
    for ((log, index) <- Seq(Seq(slickE9nLog.head), slickE9nLog.slice(1,6), slickE9nLog.slice(7, slickE9nLog.size)).flatten.zipWithIndex) {
      println(s"$index: $log")
      clientLogClassifier.classify(log, index)
    }
    println("Output message[2]:")
    dbSetup.e9nSeq.foreach(println)
    dbSetup.e9nStackTraceSeq.foreach(println)
    dbSetup.e9nDetailSeq.foreach(println)
    assert(dbSetup.e9nSeq.length === 2)
  }
}

