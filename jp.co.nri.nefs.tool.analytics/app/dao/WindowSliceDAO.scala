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
    val fut = db.run(windowSlices.sortBy(_.slice).result)
    val groupFut = fut.map(seq => seq.groupBy(_.slice))
    groupFut.map{groupMap =>
      (for {
        (slice, seq) <- groupMap
        total = seq.map(_.count).sum
        nos = seq.filter(_.windowName == "NewOrder").map(_.count).sum
        ns = seq.filter(_.windowName == "NewSplit").map(_.count).sum
        ws = WindowCountBySlice(slice, nos, ns, total - nos - ns)
      } yield ws).toSeq
    }
  }

}
