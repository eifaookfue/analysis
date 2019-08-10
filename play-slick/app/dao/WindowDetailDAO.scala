package dao

import java.io.File
import java.sql.Timestamp
import java.util.Date

import models.{ WindowDetail, Page }
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile
import scala.math._
import slick.lifted.ColumnOrdered
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import org.apache.commons.io.FileUtils
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

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

  /** Return a page of WindowDetail */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filterHandler: Option[String] = None, filterWindowName: Option[String] = None): Future[Page[WindowDetail]] = {

    val offset = pageSize * page
    //windowNameはOptional。getOrElseを使わないとvalue || is not a member of slick.lifted.Rep[_1]がでてしまう。
    //getOrElse("")とすることで、ifnull(`WINDOW_NAME`,'') like **となる
    val query1 = windowDetails.filter { windowDetail =>
      List(
        filterHandler.map(windowDetail.handler like _),
        filterWindowName.map(windowDetail.windowName.getOrElse("") like _)
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

  /*def insert(computers: Seq[Computer]): Future[Unit] =
    db.run(this.computers ++= computers).map(_ => ())
*/
  def analyze(pathname: String): Future[Unit] = {

    var windowDetailMap = Map[Option[String], ListBuffer[WindowDetail]]()

    case class FileInfo(env: String, computer: String, userName: String, startTime: String){
      val tradeDate = startTime.take(8)
    }
    def getFileInfo(fileName: String): FileInfo = {
      lazy val regex = """TradeSheet_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).log$""".r
      val regex(env, computer, userName, startTime) = fileName
      FileInfo.apply(env, computer, userName, startTime)
    }

    case class LineInfo(datetimeStr: String, logLevel: String, message: String,
                        thread: String, clazz: String){
      lazy val format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
      val datetime = new Timestamp(format.parse(datetimeStr).getTime)
    }

    def getLineInfo(line: String): LineInfo = {
      lazy val regex = """(2[0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]\s[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\.[0-9][0-9][0-9])\s\[(.*)\]\[TradeSheet\](.*)\[(.*)\]\[(j.c.*)\]$""".r
      val regex(datetimeStr, logLevel, message, thread, clazz) = line
      LineInfo.apply(datetimeStr, logLevel, message, thread, clazz)
    }

    def getWindowName(message: String, clazz: String): Option[String] = {
      lazy val regex = """\[(.*)\].*""".r
      regexOption(regex, message).orElse(Some(clazz))
    }

    def getButtonAction(message: String) : Option[String] = {
      lazy val regex = """.*\((.*)\).*""".r
      regexOption(regex, message)
    }

    def regexOption(regex: Regex, message: String):Option[String] = {
      message match {
        case regex(contents) => return Some(contents)
        case _ => return None
      }
    }

    val file = new File("D:\\tmp\\" + pathname)
    val fileInfo = getFileInfo(file.getName)
    println(fileInfo)

    val ite = FileUtils.lineIterator(file)

    var handler: String = ""
    var handlerStartTime = new Date()
    var handlerEndTime = new Date()

    ite.asScala.foreach(line => {
      val lineInfo = getLineInfo(line)
      if (lineInfo.message contains "Handler start.") {
        handlerStartTime = lineInfo.datetime
        handler = lineInfo.clazz
      } else if ((lineInfo.message contains "Dialog opened.") || (lineInfo.message contains "Opened.")) {
        val windowName = getWindowName(lineInfo.message, lineInfo.clazz)

        handlerEndTime = lineInfo.datetime
        val startupTime = handlerEndTime.getTime - handlerStartTime.getTime
        val destinationType = None
        val action = None
        val method = None
        val windowDetail = WindowDetail.apply(handler, windowName, destinationType, action, method, fileInfo.userName,
          fileInfo.tradeDate, lineInfo.datetime, startupTime)
        //たとえばNewOrderListのDialogがOpenされた後にSelect Basketが起動するケースは
        //handelerをNewOrderListとする
        handler = windowName.getOrElse("")
        windowDetailMap.get(windowName) match {
          case Some(buf) => buf += windowDetail
          case None => windowDetailMap += (windowName -> ListBuffer(windowDetail))
        }
      }
      else if ((lineInfo.message contains "Button event ends") || (lineInfo.message contains "Button Pressed")) {
        val windowName = getWindowName(lineInfo.message, lineInfo.clazz)
        val action = getButtonAction(lineInfo.message)
        windowDetailMap.get(windowName) match {
          case Some(buf) => buf.update(buf.length - 1, buf.last.copy(action = action))
          case None => println("Error")
        }
      }
    })
    ite.close
    val windowDetailList = for ((k, v) <- windowDetailMap) yield v.last
    windowDetailList.foreach(println(_))

    windowDetails.schema.create.statements.foreach(println)
    val setup = DBIO.seq(
      windowDetails.schema.create,
      windowDetails ++= windowDetailList
    )
    db.run(setup).map(_ => ())

  }

}
