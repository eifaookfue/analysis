package jp.co.nri.nefs.tool.log.analysis

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class LogActor extends Actor with ActorLogging{
  //val log = Logging(context.system, this)

  override def receive: Receive = {
    case line: String =>
      log.info(s"received $line")
    case _ =>
      log.info("received unknown message.")
  }

  def print(): Unit = {
    println("Hello")
  }
}

object Main extends LazyLogging{
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("main")
    for (i <- 0 to 10) {
      Future {
        val actor = system.actorOf(Props[LogActor], "LogActor" + i)
        for (j <- 0 to 5) {
          logger.info(s"sending $j")
          actor ! j.toString
        }
        system.stop(actor)
      }
    }
    Thread.sleep(5000)
    system.terminate()
  }
}

object Main2 {
  def main(args: Array[String]): Unit = {
    (new LogActor).print()
  }
}