package jp.co.nri.nefs.tool.analytics.store.client.classify

import jp.co.nri.nefs.tool.analytics.store.client.{LogCollection,TestingEnvironment}
import org.scalatest.FlatSpec

class DestinationTypeSpec extends FlatSpec with TestingEnvironment with LogCollection {
  "When I. A Request before close message is NewBlockDetail and" +
    "2. The previous Request is NewBlock and" +
    "3. BlockType is BLOCK, DestinationType" should "be WAVE" in {
    for ((log, index) <- waveLog.zipWithIndex) {
      clientLogClassifier.classify(log, index)
    }
    output.foreach(println)
    assert(output.head.destinationType === Some("WAVE"))
    assert(output(1).destinationType === Some("WAVE"))
    assert(output(2).destinationType === Some("WAVE"))
  }

  "When 1. A Request before close message is NewBlockDetail and " +
    "2. The previous Request is NewBlock and " +
    "3. BlockType is TRADING_LIST, DestinationType" should "be Wave" in {
    output.clear()
    for {(log, index) <- waveLog.zipWithIndex
         newLog = log.replace("BLOCK_TYPE=WAVE", "BLOCK_TYPE=TRADING_LIST")
    } {
      clientLogClassifier.classify(newLog, index)
    }
    output.foreach(println)
    assert(output.head.destinationType === Some("TRADING_LIST"))
    assert(output(1).destinationType === Some("TRADING_LIST"))
    assert(output(2).destinationType === Some("TRADING_LIST"))
  }

  "When 1. A Request before close message is NewBlockDetail and " +
    "2. The NewBlock Request dose not exist, DestinationType" should "be None" in {
    output.clear()
    for {(log, index) <- waveLog.zipWithIndex
         if !(log contains "ENewBlockProperty")
    } {
      clientLogClassifier.classify(log, index)
    }
    output.foreach(println)
    assert(output.head.destinationType === None)
    assert(output(1).destinationType === None)
    assert(output(2).destinationType === None)
  }

  "When 1. A Request before close message is NewOrderAndSlice and " +
    "2. MARKET is OSA_DERIV and" +
    "3. CROSS_TYPE is not PRINCIPAL nor AGENCY, DestinationType" should "be EXCHANGE" in {
    output.clear()
    for {(log, index) <- newOrderAndSliceDerivativeLog.zipWithIndex
    } {
      clientLogClassifier.classify(log, index)
    }
    output.foreach(println)
    assert(output(1).destinationType === Some("EXCHANGE"))
  }

  "When 1. A Request before close message is NewBasketCross and " +
    "2.MARKET is TYO_TOST and " +
    "3. CROSS_TYPE is not PRINCIPAL" should "be TOST_PRINCIPAL, DestinationType" in {
    output.clear()
    for {(log, index) <- principalBasketCross.zipWithIndex
    } {
      clientLogClassifier.classify(log, index)
    }
    output.foreach(println)
    assert(output.head.destinationType === Some("TOST_PRINCIPAL"))
  }
}