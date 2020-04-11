package jp.co.nri.nefs.tool.actor

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.Random

object AwaitTester extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val r = new Random()
    val futs = for (i <- 0 to 5) yield {
      Future {
        Thread.sleep(r.nextInt(10)*1000)
        val d = new java.util.Date()
        logger.info(d.toString)
        s"${i.toString} ${d.toString}"
      }
    }
    Thread.sleep(3000)
    logger.info(futs.mkString(","))
    val agg = Future.sequence(futs)
    val result = Await.result(agg,Duration.Inf)
    result.foreach(s => logger.info(s))
    //StdIn.readLine()
  }
}
