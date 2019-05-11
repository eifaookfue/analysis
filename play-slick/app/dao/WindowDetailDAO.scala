package dao

import java.sql.Timestamp

import models.{ WindowDetail, Page }
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait WindowDetailComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class WindowDetails(tag: Tag) extends Table[WindowDetail](tag, "WINDOW_DETAIL") {
    def handler = column[String]("HANDLER")

    def windowName = column[Option[String]]("WINDOW_NAME")

    def action = column[Option[String]]("ACTION")

    def method = column[Option[String]]("METHOD")

    def userName = column[String]("USER_NAME")

    def tradeDate = column[String]("TRADE_DATE")

    def time = column[Timestamp]("TIME")

    def startupTime = column[Long]("STARTUP_TIME")

    def * = (handler, windowName, action, method, userName, tradeDate, time, startupTime) <> (WindowDetail.tupled, WindowDetail.unapply)
  }

}
@Singleton()
class WindowDetailDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends WindowDetailComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val windowDetails = TableQuery[WindowDetails]

  /** Count all computers. */
  def count(): Future[Int] = {
    // this should be changed to
    // db.run(computers.length.result)
    // when https://github.com/slick/slick/issues/1237 is fixed
    db.run(windowDetails.map(_.windowName).length.result)
  }
  /** Count computers with a filter. */
  def count(filter: String): Future[Int] = {
    db.run(windowDetails.filter { windowDetail => windowDetail.windowName.toLowerCase like filter.toLowerCase }.length.result)
  }

  /** Return a page of WindowDetail */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[WindowDetail]] = {

    val offset = pageSize * page
    val query = if (orderBy > 0) {
      (for {
        windowDetail <- windowDetails
      } yield windowDetail)
        //.sortBy({ case (computer, id, name) => computer.name })
        .drop(offset)
        .take(pageSize)
    } else {
      (for {
        windowDetail <- windowDetails
      } yield windowDetail)
        //.sortBy({ case (computer, id, name) => computer.name.desc })
        .drop(offset)
        .take(pageSize)
    }

    for {
      totalRows <- count(filter)
      //list = query.result.map { rows => rows.collect { case (computer, id, Some(name)) => (computer, Company(id, name)) } }
      list = query.result
      result <- db.run(list)
    } yield Page(result, page, offset, totalRows)
  }

}
