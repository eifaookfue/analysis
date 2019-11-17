
package jp.co.nri.nefs.tool.json

import play.api.libs.json.{Json, Writes}

case class IOI(symbol: String, ioiqty: Double, ioiqualifiers: String)
object JsonTest {


  def main(args: Array[String]): Unit = {
    println(json)



  }

  implicit val ioiWrites = new Writes[IOI] {
    def writes(ioi: IOI) = Json.obj(
        "symbol" -> ioi.symbol,
      "ioiqty" -> ioi.ioiqty,
        "ioiquolifier" -> ioi.ioiqualifiers
    )
  }

  /*implicit val ioiWrites = new Writes[Seq[IOI]] {
    def writes(iois: Seq[IOI]) = {
      val jsons = iois.map(ioi => {
        Json.obj(
          "symbol" -> ioi.symbol,
          "ioiquolifier" -> ioi.ioiqualifiers
        )
      })
      JsArray(jsons)
    }
  }*/


  val ioi = Seq(IOI("6758", 1000,"ABC"), IOI("6502", 2000,"DEF"))
  val json = Json.toJson(ioi)
}