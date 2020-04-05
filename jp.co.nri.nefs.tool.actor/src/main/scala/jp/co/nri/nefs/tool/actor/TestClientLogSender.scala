package jp.co.nri.nefs.tool.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.{BoundedMessageQueueSemantics, RequiresMessageQueue}
import com.typesafe.scalalogging.LazyLogging
import akka.pattern.ask
import akka.routing.{FromConfig, RoundRobinPool}
import akka.pattern.gracefulStop

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

class FileSendActor extends Actor with RequiresMessageQueue[BoundedMessageQueueSemantics] with ActorLogging {

  override def preStart(): Unit = {
    log.info("preStart called")
  }

  override def postStop(): Unit = {
    log.info("postStop called")
  }

  override def receive: Receive = {
    case Manager.Start =>
      val lineSenderActor = context.actorOf(FromConfig.props(Props[LineSenderActor]), "lineSender")
      for (i <- 0 to 10) {
        lineSenderActor ! i
      }
  }
}

class LineSenderActor extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info("preStart called")
  }

  override def postStop(): Unit = {
    log.info("postStop called")
  }

  override def receive: Receive = {

    case i: Int =>
      val clientLogClassifier = new ClientLogClassifier(i)
      val clientLogClassifierActor = context.actorOf(ClientLogClassifierActor.props(clientLogClassifier), "clientLogClassifier" + i)
      for (j <- 0 to 100){
        clientLogClassifierActor ! j
      }
      log.info("gracefulStop sent")
      val ret = gracefulStop(clientLogClassifierActor, 300.seconds, Manager.Shutdown)
      Await.result(ret, 301.seconds)
      //context stop self
  }
}


class TestClientLogSender(implicit val system: ActorSystem) extends LazyLogging {

  def start(): Unit = {
    val fileSenderActor = system.actorOf(Props[FileSendActor], "fileSender")
    fileSenderActor.ask(Manager.Start)(300.seconds)
    logger.info("completed")
  }
}

object Manager {
  case object Start
  case object Shutdown
}

object TestClientLogSender extends LazyLogging {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("clientLogSender")
    try {
      new TestClientLogSender().start()
      StdIn.readLine()
    } catch {
      case e: Exception => logger.warn("", e)
    } finally {
      system.terminate()
    }


  }
}

class ClientLogClassifierActor(clientLogClassifier: ClientLogClassifier) extends Actor
  with RequiresMessageQueue[BoundedMessageQueueSemantics] with ActorLogging {

  override def receive: Receive = {
    case i: Int =>
      clientLogClassifier.execute(i)
    case Manager.Shutdown =>
      log.info("graceful stop received")
      context.stop(self)
    case _ =>
      log.warning("unknown message")
  }

  override def preStart(): Unit = {
    log.info("preStart called")
  }

  override def postStop(): Unit = {
    log.info("postStop called")
  }
}

object ClientLogClassifierActor {
  def props(clientLogClassifier: ClientLogClassifier): Props = {
    Props(new ClientLogClassifierActor(clientLogClassifier))
  }
}

class ClientLogClassifier(i: Int) extends LazyLogging{
  val buffer: ListBuffer[Int] = ListBuffer()
  def execute(j: Int): Unit = {
    buffer += j
    logger.info(s"ClientLogClassifier[$i] ${j.toString}")
    Thread.sleep(10)
  }
}