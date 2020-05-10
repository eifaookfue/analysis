package dao

import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.model.client.WindowSliceComponent
import models.WindowCountBySlice
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WindowSliceDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext )
  extends WindowSliceComponent with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  val windowSlices = TableQuery[WindowSlices]

  def list: Future[Seq[WindowCountBySlice]] = {
    val q = windowSlices.groupBy(s => (s.slice, s.windowName)).map { case ((slice, windowName), agg) =>
      (slice, windowName, agg.length)
    }.sortBy(_._1)
    val fut = db.run(q.result)
    val groupFut = fut.map(_.groupBy(_._1))
    groupFut.map { fut =>
      (for {
        (slice, seq) <- fut
        total = seq.map(_._3).sum
        nos = seq.filter(_._2.contains("NewOrder")).map(_._3).sum
        ns = seq.filter(_._2.contains("NewSplit")).map(_._3).sum
        ws = WindowCountBySlice(slice, nos, ns, total - nos - ns)
      } yield ws).toSeq
    }
  }

}
