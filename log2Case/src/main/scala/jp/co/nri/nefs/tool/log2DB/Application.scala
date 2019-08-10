package jp.co.nri.nefs.tool.log2DB

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.nio.file.{Files, Paths}
import java.sql.Timestamp
import java.util.Date

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

case class WindowDetail( appName: String, computerName: String, userId:String, tradeDate: String, lineNo: Long,
                         handler: String, windowName: Option[String], destinationType: Option[String],
                         action: Option[String], method: Option[String],
                         time: Timestamp, startupTime: Long)

object Application {

  /**
    * キー：windowName
    * バリュー：Windowクラスのリスト。追加する必要があるためmutableなListBufferを用いる
    */
  private var windowDetailMap = Map[Option[String], ListBuffer[WindowDetail]]()

  private case class FileInfo(appName: String, env: String, computer: String, userId: String, startTime: String){
    val tradeDate = startTime.take(8)
  }
  private def getFileInfo(fileName: String): FileInfo = {
    lazy val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).log$""".r
    val regex(appName, env, computer, userId, startTime) = fileName
    FileInfo.apply(appName, env, computer, userId, startTime)
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

  //java.io.EOFExceptionが出るが無視する
  private def using[A <: java.io.Closeable](s: A)(f: A => Unit): Unit = {
    try { f(s) } catch {case e: Exception => } finally { s.close() }
  }

  def main(args: Array[String]): Unit = {
    val path = Paths.get(args(0))
    val fileInfo = getFileInfo(path.getFileName.toString)

    var handler: String = ""
    var handlerStartTime = new Date()
    var handlerEndTime = new Date()

    var lineNo = 1L
    Files.readAllLines(path).forEach(line => {
      val lineInfo = getLineInfo(line)
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
        val windowDetail = WindowDetail.apply(fileInfo.appName, fileInfo.computer, fileInfo.userId,
          fileInfo.tradeDate, lineNo, handler, windowName, destinationType, action, method,
          lineInfo.datetime, startupTime)
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
      lineNo = lineNo + 1
    })
    //型を変換しないとIterableとなりSortができない
    val tmpList: List[WindowDetail] = (for ((k, v) <- windowDetailMap) yield v.last)(collection.breakOut)
    val windowDetailList = tmpList.sortBy(_.lineNo)
    val outpath = Paths.get("D:\\tmp\\a.out")
    val ostream = new ObjectOutputStream(Files.newOutputStream(outpath))
    //    os.writeObject(windowDetailList)
    using(ostream){ os =>
      windowDetailList.foreach( w => {
        os.writeObject(w)
      })
    }
    val istream = new ObjectInputStream(Files.newInputStream(outpath))
    using(istream){is =>
      Iterator.continually(is.readObject()).takeWhile(_ != null).foreach(println _)
    }
  }
}