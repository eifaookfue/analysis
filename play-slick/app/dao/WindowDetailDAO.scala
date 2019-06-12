package dao

import java.sql.Timestamp

import models.{ WindowDetail, Page }
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile
import scala.math._
import slick.lifted.ColumnOrdered
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.concurrent.{ExecutionContext, Future}

trait WindowDetailComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class WindowDetails(tag: Tag) extends Table[WindowDetail](tag, "WINDOW_DETAIL") {
    def handler = column[String]("HANDLER")

    def windowName = column[Option[String]]("WINDOW_NAME")

    def destinationType = column[Option[String]]("DESTINATION_TYPE")

    def action = column[Option[String]]("ACTION")

    def method = column[Option[String]]("METHOD")

    def userName = column[String]("USER_NAME")

    def tradeDate = column[String]("TRADE_DATE")

    def time = column[Timestamp]("TIME")

    def startupTime = column[Long]("STARTUP_TIME")

    def * = (handler, windowName, destinationType, action, method, userName, tradeDate, time, startupTime) <> (WindowDetail.tupled, WindowDetail.unapply)
    //def nth = Vector(handler, windowName, action, method, userName, tradeDate, time, startupTime )
    // 返り値はAnyでもいいが、ColumnOrderedとしてみた。
    def getSortedColumn(i : Int) : ColumnOrdered[_] = {
      i match {
        case 1 => handler.asc
        case -1 => handler.desc
        case 2 => windowName.asc.nullsFirst
        case -2 => windowName.desc.nullsFirst
        case 3 => destinationType.asc.nullsFirst
        case -3 => destinationType.desc.nullsFirst
        case 4 => action.asc.nullsFirst
        case -4 => action.desc.nullsFirst
        case 5 => method.asc.nullsFirst
        case -5 => method.desc.nullsFirst
        case 6 => userName.asc
        case -6 => userName.desc
        case 7 => tradeDate.asc
        case -7 => tradeDate.desc
        case 8 => time.asc
        case -8 => time.desc
        case 9 => startupTime.asc
        case -9 => startupTime.desc
        case _ => handler.asc
      }
    }

    //def nth = handler
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
  def count(filterHandler: Option[String], filterWindowName: Option[String]): Future[Int] = {
    db.run(windowDetails.filter { windowDetail => windowDetail.windowName.toLowerCase like filterHandler.map(_.toLowerCase).getOrElse("") }.length.result)
  }

  /*def baseQuery(filterHandler: String, filterWindowName: String): Query[_,_,_] = {
    val criteriaHandler = Some(filterHandler)
    val criteriaWindowName = Some(filterWindowName)
    windowDetails.filter { windowDetail =>
      List(
        criteriaHandler.map(windowDetail.handler like _),
        criteriaWindowName.map(windowDetail.windowName.getOrElse("") like _)
      ).collect({ case Some(criteria) => criteria }).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
    }
  }*/

  /** Return a page of WindowDetail */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filterHandler: Option[String] = None, filterWindowName: Option[String] = None): Future[Page[WindowDetail]] = {

    val offset = pageSize * page
    //windowNameはOptional。getOrElseを使わないとvalue || is not a member of slick.lifted.Rep[_1]がでてしまう。
    //getOrElse("")とすることで、ifnull(`WINDOW_NAME`,'') like **となる
    val query = windowDetails.filter { windowDetail =>
      List(
        filterHandler.map(windowDetail.handler like _),
        filterWindowName.map(windowDetail.windowName.getOrElse("") like _)
      ).collect({ case Some(criteria) => criteria }).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
    }.sortBy(_.getSortedColumn(orderBy))
      .drop(offset)
      .take(pageSize)

    for {
      totalRows <- count(filterHandler, filterWindowName)
      //list = query.result.map { rows => rows.collect { case (computer, id, Some(name)) => (computer, Company(id, name)) } }
      list = query.result
      result <- db.run(list)
    } yield Page(result, page, offset, totalRows)
  }

  /*def insert(computers: Seq[Computer]): Future[Unit] =
    db.run(this.computers ++= computers).map(_ => ())
*/
  def initialize(windowDetailList : List[WindowDetail]): Unit = {

    try {
      windowDetails.schema.create.statements.foreach(println)
      val setup = DBIO.seq(
        windowDetails.schema.create,
        windowDetails ++= windowDetailList
      )
      val setupFuture = db.run(setup)
      Await.result(setupFuture, Duration.Inf)
    } finally db.close

  }

}
