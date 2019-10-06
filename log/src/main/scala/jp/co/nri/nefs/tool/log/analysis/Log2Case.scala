package jp.co.nri.nefs.tool.log.analysis

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.nio.file.{Files, Path, Paths}
import java.sql.Timestamp
import java.util.Date
import jp.co.nri.nefs.tool.log.common.model.WindowDetail
import jp.co.nri.nefs.tool.log.common.utils.FileUtils._
import jp.co.nri.nefs.tool.log.common.utils.RegexUtils._
import jp.co.nri.nefs.tool.log.common.utils.ZipUtils._
import org.apache.poi.ss.usermodel._

import scala.collection.JavaConverters._
import scala.collection._
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

class Log2Case(outputdir: Path) {

  /**
    * キー：windowName
    * バリュー：Windowクラスのリスト。追加する必要があるためmutableなListBufferを用いる
    */
  private var windowDetailMap = Map[Option[String], ListBuffer[WindowDetail]]()

  private case class FileInfo(appName: String, env: String, computer: String, userId: String, startTime: String){
    val tradeDate: String = startTime.take(8)
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
      case regex(contents) => Some(contents)
      case _ => None
    }
  }

  //java.io.EOFExceptionが出るが無視する
  private def using[A <: java.io.Closeable](s: A)(f: A => Unit): Unit = {
    try { f(s) } catch {case _: Exception => } finally { s.close() }
  }

  def execute(paths: List[Path]): Unit = {
    paths.foreach(p => execute(p))
  }

  def execute(path: Path): Unit = {

    import Utils._
    if (isZipFile(path)){
      val expandedDir = unzip(path)
      val paths = for (file <- Files.list(expandedDir).iterator().asScala.toList) yield file
      execute(paths)
      delete(expandedDir)
    } else {
      //val fileInfo = getFileInfo(path.getFileName.toString)
      val fileInfo =
        getOMSAplInfo(path.getFileName.toString) match {
          case Some(f) => f
          case None =>
            println("not valid format")
            return
        }

      var handler: String = ""
      var handlerStartTime = new Date()
      var handlerEndTime = new Date()

      var lineNo = 1L
      Files.lines(path).forEach(line => {
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
            lineInfo.datetime, startupTime, path.toString)
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
      //val tmpList: List[WindowDetail] = (for ((k, v) <- windowDetailMap) yield v.last)(collection.breakOut)
      //なぜかIterable[jp.co.nri.nefs.tool.log.common.model.WindowDetail] does not take parametersというエラーがでてしまう
      val tmp = for ((_, v) <- windowDetailMap) yield v.last
      val tmpList = tmp.toList
      val windowDetailList = tmpList.sortBy(_.lineNo)
      val outpath = outputdir.resolve(getObjFile(path.getFileName.toFile.toString))
      val ostream = new ObjectOutputStream(Files.newOutputStream(outpath))
      //    os.writeObject(windowDetailList)
      using(ostream){ os =>
        windowDetailList.foreach( w => {
          os.writeObject(w)
        })
      }
      val istream = new ObjectInputStream(Files.newInputStream(outpath))
      using(istream){is =>
        Iterator.continually(is.readObject()).takeWhile(_ != null).foreach(v => println(v))
      }
    }

  }

}

object Utils {
  def getObjFile(name: String): String = {
    getBase(name) + ".obj"
  }
  def getBase(name: String): String = {
    val index = name.lastIndexOf('.')
    if (index != -1)
      name.substring(0, index)
    else
      name
  }

}


object Log2Case {
  type OptionMap = Map[Symbol, String]
  val usage = """
        Usage: jp.co.nri.nefs.tool.log.analysis.Log2Case [--searchdir dir | --file file | --excelFile file] --outputdir dir
        """

  sealed trait EExecutionType
  case object LOGDIR extends EExecutionType
  case object LOGFILE extends EExecutionType
  case object EXCELFILE extends EExecutionType



  def main(args: Array[String]): Unit = {

    //lazy val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).log$""".r
    lazy val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).(log|zip)$""".r
    val options = nextOption(Map(), args.toList)
    val (executionType, dirOrFile, outputdir) = getOption(options)
    val log2case = new Log2Case(outputdir)
    executionType match {
      case LOGDIR =>
        val paths = for {
          path <- Files.walk(dirOrFile).iterator().asScala.toList
          if path.toFile.isFile
          if path.getFileName.toString.matches(regex.regex)
        } yield path
        log2case.execute(paths)
      case LOGFILE =>
        log2case.execute(dirOrFile)
      case EXCELFILE =>
        val excel2Case = new Excel2Case(outputdir)
        excel2Case.execute(dirOrFile)
    }
  }

  /**
    *
    * @param options オプションのマップ
    * @return searchdirが指定されているか
    */
  def getOption(options: OptionMap): (EExecutionType, Path, Path) = {
    val searchdir = options.get(Symbol("searchdir")).map(Paths.get(_))
    val file = options.get(Symbol("file")).map(Paths.get(_))
    val excelFile = options.get(Symbol("excelFile")).map(Paths.get(_))
    val outputdir = options.get(Symbol("outputdir")).map(Paths.get(_))

    val executionType = if (searchdir.isDefined && file.isEmpty && excelFile.isEmpty) LOGDIR
      else if (file.isDefined && searchdir.isEmpty && excelFile.isEmpty) LOGFILE
      else if (excelFile.isDefined && searchdir.isEmpty && file.isEmpty) EXCELFILE
      else {
        println(usage)
        throw new java.lang.IllegalArgumentException
      }
    if (outputdir.isEmpty) {
      println(usage)
      throw new java.lang.IllegalArgumentException
    }
    (executionType, searchdir.getOrElse(file.getOrElse(excelFile.get)), outputdir.get)
  }

  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    list match {
      case Nil => map
      case "--searchdir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("searchdir") -> value), tail)
      case "--file" :: value :: tail =>
        nextOption(map ++ Map(Symbol("file") -> value), tail)
      case "--excelFile" :: value :: tail =>
        nextOption(map ++ Map(Symbol("excelFile") -> value), tail)
      case "--outputdir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("outputdir") -> value), tail)
      case _ => println("Unknown option")
        println(usage)
        sys.exit(1)
    }
  }
}

class Excel2Case(outputdir: Path) {
  import jp.co.nri.nefs.tool.log.common.utils.RichCell.cellToRichCell
  import Utils._
  def execute(path: Path): Unit = {
    val book = WorkbookFactory.create(path.toFile)
    //val sheet = book.getSheet("Sheet1")
    val sheets = for {
      no <- 0 until book.getNumberOfSheets
      name = book.getSheetName(no)
      if name.contains("WindowDetail")
      sheet = book.getSheetAt(no)
    } yield sheet
    val windowDetails = for {
      sheet <- sheets
      (row, index) <- sheet.iterator().asScala.zipWithIndex
      if index > 0 //先頭行スキップ
      iterator = row.iterator()
      appName = iterator.next().getValue[String].get
      computerName = iterator.next().getValue[String].get
      userId = iterator.next().getValue[String].get
      tradeDate = iterator.next().getValue[String].get
      lineNo = iterator.next().getValue[Long].get
      handler = iterator.next().getValue[String].get
      windowName = iterator.next().getValue[String]
      destinationType = iterator.next().getValue[String]
      action = iterator.next().getValue[String]
      method = iterator.next().getValue[String]
      time = iterator.next().getValue[Timestamp].get
      startupTime = iterator.next().getValue[Long].get
      logFile = iterator.next().getValue[String].get
      windowDetail = WindowDetail(appName,computerName,userId,tradeDate, lineNo,
        handler, windowName, destinationType, action, method, time, startupTime, logFile)
    } yield windowDetail

    val outpath = outputdir.resolve(getObjFile(path.getFileName.toFile.toString))
    val ostream = new ObjectOutputStream(Files.newOutputStream(outpath))
    using(ostream){ os =>
      windowDetails.foreach( w => {
        os.writeObject(w)
      })
    }
    val istream = new ObjectInputStream(Files.newInputStream(outpath))
    using(istream){is =>
      Iterator.continually(is.readObject()).takeWhile(_ != null).foreach(v => println(v))
    }
  }
}