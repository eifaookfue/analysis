package jp.co.nri.nefs.tool.analytics.store.client

import org.scalatest.FlatSpec

class H2LogStoreSpec extends FlatSpec with H2Environment {

  "A" should "be" in {
    clientLogStore.recreate

  }

}