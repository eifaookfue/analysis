
package jp.co.nri.nefs.tool.json

import play.api.libs.json.{JsArray, Json, Writes}
import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.ws._
import play.api.libs.ws.ahc._

case class IOI(symbol: String, ioiqualifiers: String)
case class IOIS(iois: Seq[IOI])
object JsonTest {
  import scala.concurrent.ExecutionContext.Implicits._

  def call(wsClient: StandaloneWSClient): Future[Unit] = {
    wsClient.url("http://www.google.com").get().map { response ⇒
      val statusText: String = response.statusText
      val body = response.body[String]
      println(s"Got a response $statusText")
    }
  }

  def main(args: Array[String]): Unit = {
    println(json)
    import DefaultBodyReadables._
    import scala.concurrent.ExecutionContext.Implicits._
    system.registerOnTermination {
      System.exit(0)
    }
    implicit val materializer = Materializer.matFromSystem
    val wsClient = StandaloneAhcWSClient()
    call(wsClient)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }



  }

  /*implicit val ioiWrites = new Writes[IOI] {
    def writes(ioi: IOI) = Json.obj(
        "symbol" -> ioi.symbol,
        "ioiquolifier" -> ioi.ioiqualifiers
    )
  }*/

  implicit val ioiWrites = new Writes[Seq[IOI]] {
    def writes(iois: Seq[IOI]) = {
      val jsons = iois.map(ioi => {
        Json.obj(
          "symbol" -> ioi.symbol,
          "ioiquolifier" -> ioi.ioiqualifiers
        )
      })
      JsArray(jsons)
    }
  }


  val ioi = Seq(IOI("6758", "ABC"), IOI("6502", "DEF"))
  val json = Json.toJson(ioi)
}