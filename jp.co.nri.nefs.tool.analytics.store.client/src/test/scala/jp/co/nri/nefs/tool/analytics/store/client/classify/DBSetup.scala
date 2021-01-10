package jp.co.nri.nefs.tool.analytics.store.client.classify

import com.google.inject.ImplementedBy
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.client._
import jp.co.nri.nefs.tool.analytics.store.client.{H2Environment, LogCollection}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@ImplementedBy(classOf[DefaultDBSetup])
trait DBSetup {
  def initialize(): Unit
  def e9nSeq: Seq[E9n]
  def e9nStackTraceSeq: Seq[E9nStackTrace]
  def e9nDetailSeq: Seq[E9nDetail]
}

class DefaultDBSetup @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends DBSetup
  with H2Environment with LogCollection
  with E9nComponent with E9nStackTraceComponent with E9nDetailComponent
  with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val e9ns = TableQuery[E9ns]
  val e9nStackTraces = TableQuery[E9nStackTraces]
  val e9nDetails = TableQuery[E9nDetails]

  override def initialize(): Unit = {
    val f1 = db.run(DBIO.seq(e9ns.schema.truncate, e9nStackTraces.schema.truncate, e9nDetails.schema.truncate))
    Await.result(f1, Duration.Inf)
    val headMessage = slickE9nLog(1)
    val e9nStackTraceSeq =
      for {(msg, number) <- Seq(slickE9nLog.slice(1,4), slickE9nLog.slice(7, slickE9nLog.length - 1)).flatten.zipWithIndex}
      yield E9nStackTrace(1, number, msg)
    val e9nInsert = e9ns.map(_.e9nProjection) += E9n(1, headMessage, e9nStackTraceSeq.map(_.message.trim).mkString.length)
    val e9nStackTraceInsert = e9nStackTraces.map(_.e9nStackTraceProjection) ++= e9nStackTraceSeq
    val f2 = db.run(DBIO.seq(e9nInsert, e9nStackTraceInsert))
    Await.result(f2, Duration.Inf)
  }

  override def e9nSeq: Seq[E9n] = {
    Await.result(db.run(e9ns.map(_.e9nProjection).result), Duration.Inf)
  }

  override def e9nStackTraceSeq: Seq[E9nStackTrace] = {
    Await.result(db.run(e9nStackTraces.map(_.e9nStackTraceProjection).result), Duration.Inf)
  }

  override def e9nDetailSeq: Seq[E9nDetail] = {
    Await.result(db.run(e9nDetails.map(_.e9nDetailProjection).result), Duration.Inf)
  }

}