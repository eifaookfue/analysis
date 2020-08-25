package jp.co.nri.nefs.tool.analytics.store.client.record

import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.model.client._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class DBAccessor  @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends LogComponent with WindowDetailComponent with PreCheckComponent
    with E9nComponent with E9nStackTraceComponent with E9nDetailComponent with E9nCountComponent
    with LazyLogging
    with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val dbProfile: JdbcProfile = profile
  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val preChecks = TableQuery[PreChecks]
  val e9ns = TableQuery[E9ns]
  val e9nStackTraces = TableQuery[E9nStackTraces]
  val e9nDetails = TableQuery[E9nDetails]
  val e9nCounts = TableQuery[E9nCounts]

  def execute(): Unit = {
    query(logs.filter(_.logId === 1).map(_.appName))
  }

  def query[T](q2:  Query[Rep[T], T, scala.Seq]): Seq[T] = {
    //val q1 = logs.filter(_.logId === 1).map(_.appName)
    val f1 = db.run(q2.result)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(value) => value
      case Failure(exception) => throw exception
    }
  }

}
