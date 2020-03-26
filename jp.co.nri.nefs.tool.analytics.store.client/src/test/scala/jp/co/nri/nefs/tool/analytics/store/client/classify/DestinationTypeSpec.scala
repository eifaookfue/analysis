package jp.co.nri.nefs.tool.analytics.store.client.classify

import jp.co.nri.nefs.tool.analytics.store.client.{LogCollection, TestingEnvironment}
import org.scalatest.FlatSpec

class DestinationTypeSpec extends FlatSpec with TestingEnvironment with LogCollection{

  "When destinationType" should "a" in {
    for ((line, index) <- destinationTypeNewSplitLog.zipWithIndex) {
      clientLogClassifier.classify(line, index)
    }
    output.foreach(println)
  }
}
