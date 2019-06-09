package jp.co.nri.nefs.tool.apllog

import java.io.File
import java.sql.Timestamp
import java.util.Date

import scala.collection.JavaConverters._
import org.apache.commons.io.FileUtils

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import com.typesafe.scalalogging.LazyLogging
import slick.driver.MySQLDriver.api._
//import slick.jdbc.JdbcProfile
import models.WindowDetail
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import dao.WindowDetailDAO
import play.api.db.slick.SlickModule
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

class Application @Inject() (
                              windowDetailDao : WindowDetailDAO
                            ){

}


object Application extends LazyLogging{

  class WindowDetails2(tag: Tag) extends Table[WindowDetail](tag, "WINDOW_DETAIL") {
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

    val details = TableQuery[WindowDetails2]

  }

  val windowDetails = TableQuery[WindowDetails2]
  /**
    * キー：windowName
    * バリュー：Windowクラスのリスト。追加する必要があるためmutableなListBufferを用いる
    */
  private var windowDetailMap = Map[Option[String], ListBuffer[WindowDetail]]()

  private case class FileInfo(env: String, computer: String, userName: String, startTime: String){
    val tradeDate = startTime.take(8)
  }
  private def getFileInfo(fileName: String): FileInfo = {
    lazy val regex = """TradeSheet_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).log$""".r
    val regex(env, computer, userName, startTime) = fileName
    FileInfo.apply(env, computer, userName, startTime)
  }

  private case class LineInfo(datetimeStr: String, logLevel: String, message: String,
                              thread: String, clazz: String){
    lazy val format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
    val datetime = new Timestamp(format.parse(datetimeStr).getTime)
  }

  private def getLineInfo(line: String): LineInfo = {
    lazy val regex = """(2[0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]\s[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\.[0-9][0-9][0-9])\s\[(.*)\]\[TradeSheet\](.*)\[(.*)\]\[(j.c.*)\]$""".r
    val regex(datetimeStr, logLevel, message, thread, clazz) = line
    LineInfo.apply(datetimeStr, logLevel, message, thread, clazz)
  }
  private def getWindowName(message: String, clazz: String): Option[String] = {
    lazy val regex = """\[(.*)\].*""".r
    regexOption(regex, message).orElse(Some(clazz))
  }

  private def getButtonAction(message: String) : Option[String] = {
    lazy val regex = """.*\((.*)\).*""".r
    regexOption(regex, message)
  }

  private def regexOption(regex: Regex, message: String):Option[String] = {
    message match {
      case regex(contents) => return Some(contents)
      case _ => return None
    }
  }
  //~run D:\tmp\TradeSheet_OMS_TKY_FID2CAD332_356435_20190315090918535.log
  def main(args: Array[String]): Unit = {
    if (args.size == 0) {
      println("run fileName")
      sys.exit(-1)
    }
    val pathname = args(0)
    val file = new File(pathname)
    val fileInfo = getFileInfo(file.getName)
    println(fileInfo)

    val ite = FileUtils.lineIterator(new File(pathname))

    //val regex = """(.*)\[(.*)\]\[(.*)\](.*)\[(.*)\]\[(j.c.*)\]""".r
    //             2019            -03        -15          09        :10        :38        .  045                [OMS:INFO ][TradeSheet]
    //val regex = """(2[0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]\s[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\.[0-9][0-9][0-9])\s\[(.*)\]\[TradeSheet\](.*)\[(.*)\]\[(j.c.*)\]$""".r
    //val messageRe = """\[(.*)\].*""".r
    //val format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
    var handler: String = ""
    var handlerStartTime = new Date()
    var handlerEndTime = new Date()

    ite.asScala.foreach(line => {
      val lineInfo = getLineInfo(line)
      //val regex(datetimeStr, logLevel, message, thread, clazz) = line
      if (lineInfo.message contains "Handler start.") {
        handlerStartTime = lineInfo.datetime
        handler = lineInfo.clazz
      }
      //[New Basket]Dialog opened.[main][j.c.n.n.o.r.p.d.b.NewBasketDialog$1]
      //[TradeSheet]Opened.[main][j.c.n.n.o.r.p.d.c.QuestionDialog]
      else if ((lineInfo.message contains "Dialog opened.") || (lineInfo.message contains "Opened.")) {
        val windowName = getWindowName(lineInfo.message, lineInfo.clazz)

        //else if ("Dialog opened.".equals(message)) {
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

      //println(s.substring(0,23))
      //val re = "\s\[\s\]"
      //re.findFirstMatchIn(s).get

      //      s.split(" ") match {
      //        case Array(date, time, _*) => println(date)
      //      }

      //      val ss = s.split(" ")
      //      val d = format.parse(ss(0))
      //      println(d)
    })
    ite.close


    val windowDetailList = for ((k, v) <- windowDetailMap) yield v.last
    windowDetailList.foreach(println(_))

    // いったんあきらめる
    //val module = new SlickModule()
    //@Inject()
    //var dbConfigProvider : DatabaseConfigProvider
    //val windowDetailDao = new WindowDetailDAO(dbConfigProvider)


    windowDetailDao.initialize(windowDetailList)

    val db = Database.forConfig("mydb")
    try {
      windowDetails.schema.create.statements.foreach(println)
      val setup = DBIO.seq(
        windowDetails.schema.create,
        windowDetails ++= windowDetailList
      )
      val setupFuture = db.run(setup)
      Await.result(setupFuture, Duration.Inf)
    }finally db.close


  println("end")




  }
}
