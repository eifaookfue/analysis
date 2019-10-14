package dao

import java.io.{EOFException, ObjectInputStream}
import java.nio.file.{Files, Path, Paths}
import java.sql.Timestamp

import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.log.common.model.WindowDetail
import models.{Page,Params}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ColumnOrdered
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

trait WindowDetailComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class WindowDetails(tag: Tag) extends Table[WindowDetail](tag, "WINDOW_DETAIL") {
    def appName = column[String]("APPLICATION_NAME", O.Length(20))
    def computerName = column[String]("COMPUTER_NAME", O.Length(20))
    def userId = column[String]("USERID", O.Length(6))
    def tradeDate = column[String]("TRADE_DATE", O.Length(8))
    def lineNo = column[Long]("LINE_NO", O.Length(20))
    def handler = column[String]("HANDLER")
    def windowName = column[Option[String]]("WINDOW_NAME")
    def destinationType = column[Option[String]]("DESTINATION_TYPE")
    def action = column[Option[String]]("ACTION")
    def method = column[Option[String]]("METHOD")
    def time = column[Timestamp]("TIME")
    def startupTime = column[Long]("STARTUP_TIME")
    def logFile = column[String]("LOGFILE")
    def * = (appName, computerName, userId, tradeDate, lineNo, handler, windowName, destinationType, action, method, time, startupTime, logFile) <> (WindowDetail.tupled, WindowDetail.unapply)
    def idx_1 = index("idx_1", (appName, computerName, userId, tradeDate, lineNo), unique = true)
    //def nth = Vector(handler, windowName, action, method, userName, tradeDate, time, startupTime )
    // 返り値はAnyでもいいが、ColumnOrderedとしてみた。
    def getSortedColumn(i : Int) : ColumnOrdered[_] = {
      i match {
        case 1 => appName.asc
        case -1 => appName.desc
        case 2 => computerName.asc
        case -2 => computerName.desc
        case 3 => userId.asc
        case -3 => userId.desc
        case 4 => tradeDate.asc
        case -4 => tradeDate.desc
        case 5 => lineNo.asc
        case -5 => lineNo.desc
        case 6 => handler.asc
        case -6 => handler.desc
        case 7 => windowName.asc.nullsFirst
        case -7 => windowName.desc.nullsFirst
        case 8 => destinationType.asc.nullsFirst
        case -8 => destinationType.desc.nullsFirst
        case 9 => action.asc.nullsFirst
        case -9 => action.desc.nullsFirst
        case 10 => method.asc.nullsFirst
        case -10 => method.desc.nullsFirst
        case 11 => time.asc
        case -11 => time.desc
        case 12 => startupTime.asc
        case -12 => startupTime.desc
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


  /** Return a page of WindowDetail */
  def list(params: Params, pageSize: Int = 10): Future[Page[WindowDetail]] = {
    val page = params.page
    val orderBy = params.orderBy.getOrElse(1)
    val offset = page * pageSize
    //windowNameはOptional。getOrElseを使わないとvalue || is not a member of slick.lifted.Rep[_1]がでてしまう。
    //getOrElse("")とすることで、ifnull(`WINDOW_NAME`,'') like **となる
    //画面のサーチボックスに何も入力しなかった場合、NoneではなくSome()が送られてきてしまうため、Option型に変更
    val query1 = windowDetails.filter { windowDetail =>
      List(
        params.appName.filter(_.trim.nonEmpty).map(windowDetail.appName like "%" + _ +  "%"),
        params.computerName.filter(_.trim.nonEmpty).map(windowDetail.computerName like "%" + _ +  "%"),
        params.userId.filter(_.trim.nonEmpty).map(windowDetail.userId like "%" + _ +  "%"),
        params.tradeDate.filter(_.trim.nonEmpty).map(windowDetail.tradeDate like "%" + _ +  "%"),
        params.lineNo.map(windowDetail.lineNo === _),
        params.handler.filter(_.trim.nonEmpty).map(windowDetail.handler like "%" + _ +  "%"),
        params.windowName.filter(_.trim.nonEmpty).map(windowDetail.windowName.getOrElse("") like "%" + _ +  "%"),
        params.destinationType.filter(_.trim.nonEmpty).map(windowDetail.destinationType.getOrElse("") like "%" + _ +  "%"),
        params.action.filter(_.trim.nonEmpty).map(windowDetail.action.getOrElse("") like "%" + _ +  "%"),
        params.method.filter(_.trim.nonEmpty).map(windowDetail.method.getOrElse("") like "%" + _ +  "%"),
        params.time.map(windowDetail.time === _),
        params.startupTime.map(windowDetail.startupTime === _)
      ).collect({ case Some(criteria) => criteria }).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
    }
    val query2 = query1.sortBy(_.getSortedColumn(orderBy))
        .drop(offset)
        .take(pageSize)

    for {
      totalRows <- db.run(query1.length.result)
      //list = query.result.map { rows => rows.collect { case (computer, id, Some(name)) => (computer, Company(id, name)) } }
      list = query2.result
      result <- db.run(list)
    } yield Page(result, page, offset, totalRows)
  }

  private def using[A <: java.io.Closeable, B](s: A)(f: A => B): B = {
    try { f(s) } finally { s.close() }
  }

  private def readWindowDetail(inputstream: ObjectInputStream): WindowDetail = {
    try {
      inputstream.readObject().asInstanceOf[WindowDetail]
    } catch { case _ : EOFException => null }
  }

  private def createWindowDetailListByFile(path: Path): List[WindowDetail] = {
    val istream = new ObjectInputStream(Files.newInputStream(path))
    using(istream) { is =>
      Iterator.continually(readWindowDetail(is)).takeWhile(_ != null).toList
    }
  }

  private def createWindowDetailListByDir(path: Path): List[WindowDetail] = {
    val files = Files.list(path).iterator().asScala.toList
    for ( f <- files; w <- createWindowDetailListByFile(f) ) yield w
  }

  private def recreate(): Unit = {
    val schema = windowDetails.schema
    schema.create.statements.foreach(println)
    val setup = DBIO.seq(
      schema.dropIfExists,
      schema.createIfNotExists
    )
    val setupFuture = db.run(setup)
    Await.result(setupFuture, Duration.Inf)
  }

  def load(name: String, isRecreate: Boolean): Future[Unit] = {

    if (isRecreate) {
      recreate()
    }

    val path = Paths.get("D:\\", name)
    val windowDetailList = createWindowDetailListByDir(path)
    //これでうまくいくが一意制約エラーがでたときに、そこで止まってしまう
    //db.run(windowDetails ++= windowDetailList).map(_ => ())

/*    for {
      windowDetail <- windowDetailList
      setupFuture = db.run(windowDetails += windowDetail)
      Await.result(setupFuture, Duration.Inf)
    }*/
    windowDetailList.foreach(windowDetail => {
      try {
        val setupFuture = db.run(windowDetails += windowDetail)
        Await.result(setupFuture, Duration.Inf)
      } catch {
        case e: Exception => println(e)
      }
    })
    Future{}
  }

}
