package jp.co.nri.nefs.tool.elp

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.http.javadsl.model.ws.Message
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.Materializer
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{Future, Promise}

case class UserPass(userName: String, password: String)

object WebSocketClient extends LazyLogging {
  def main(args: Array[String]) = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer.matFromSystem
    val config = ConfigFactory.load()
    val baseAddr = config.getString("baseAddr")
    val subscribeURL = "ws://" + baseAddr + "/subscribe-ioi"
    implicit val userPass: UserPass = UserPass(config.getString("websocketUser"), config.getString("websocketPassword"))
    subscribe(subscribeURL)
  }

  def subscribe(url: String)(implicit system: ActorSystem, materializer: Materializer, userPass: UserPass): Unit = {
    import system.dispatcher

    val flow: Flow[Message, Message, Promise[Option[Message]]] =
      Flow.fromSinkAndSourceMat(
        Sink.foreach[Message] {
          case strictMessage: TextMessage.Strict => logger.info("received strict message"); println(strictMessage.text)
          case streamedMessage: TextMessage.Streamed => logger.info("received streamed message"); streamedMessage.textStream.runForeach(println)
          case m => println("other" + m)
        },
        Source.maybe[Message])(Keep.right)

    val (upgradeResponse, promise) = Http().singleWebSocketRequest(WebSocketRequest(
      url,
      extraHeaders = scala.collection.immutable.Seq(Authorization(BasicHttpCredentials(userPass.userName, userPass.password)).asInstanceOf[HttpHeader])),
      flow)

    val connected = upgradeResponse.map { upgrade =>
      // just like a regular http request we can access response status which is available via upgrade.response.status
      // status code 101 (Switching Protocols) indicates that server support WebSockets
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Done
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }
    // in a real application you would not side effect here
    // and handle errors more carefully
    connected.onComplete(println)

    //promise.success(None)
  }
}
