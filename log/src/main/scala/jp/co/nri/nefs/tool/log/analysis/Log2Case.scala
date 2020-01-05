package jp.co.nri.nefs.tool.log.analysis

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import java.sql.Timestamp

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.log.common.model.{Log, WindowDetail}
import jp.co.nri.nefs.tool.log.common.utils.FileUtils
import jp.co.nri.nefs.tool.log.common.utils.FileUtils._
import jp.co.nri.nefs.tool.log.common.utils.RegexUtils._
import jp.co.nri.nefs.tool.log.common.utils.ZipUtils._
import org.apache.poi.ss.usermodel._

import scala.collection.JavaConverters._
import scala.collection._
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex


trait AnalysisReporterComponent {
  val analysisReporterFactory: AnalysisReporterFactory

  class AnalysisReporterFactory {
    private var outputDir: Path = _
    def setOutputDir(outputDir: Path): Unit = {
      this.outputDir = outputDir
    }
    def create(fileName: String): AnalysisReporter = {
      val reporter = new AnalysisReporter
      reporter.setOutputDir(outputDir)
      reporter.setFileName(fileName)
    }
  }

  class AnalysisReporter extends LazyLogging {

    import jp.co.nri.nefs.tool.log.common.utils.RichFiles.stringToRichString

    private var outputDir: Path = _
    private var fileName: String = _
    private lazy val outLogPath = outputDir.resolve(fileName.basename + "Log" + Keywords.OBJ_EXTENSION)
    private lazy val logOutputStream = new ObjectOutputStream(Files.newOutputStream(outLogPath))
    private lazy val outDetailPath = outputDir.resolve(fileName.basename + "Detail" + Keywords.OBJ_EXTENSION)
    private lazy val detailOutputStream = new ObjectOutputStream(Files.newOutputStream(outDetailPath))

    def setOutputDir(outputDir: Path): Unit = {
      this.outputDir = outputDir
    }

    def setFileName(fileName: String): Unit = {
      this.fileName = fileName
    }

    def report(log: Log): Unit = {
      doReport(log, logOutputStream)
    }

    def report(detail: WindowDetail): Unit = {
      doReport(detail, detailOutputStream)
    }

    private def doReport[T](obj: T, stream: ObjectOutputStream): Unit = {
      try {
        logger.info(obj.toString)
        stream.writeObject(obj)
      } catch {
        case _: Exception =>
      }
    }
  }
}

class Log2Case(outputdir: Path) extends  LazyLogging {

  implicit val config: Config = ConfigFactory.load()

  private trait Naming {
    val name: String
  }

  private trait Starting {
    val start: LineTime
  }

  private trait Ending {
    val end: Option[LineTime]
  }

  private case class LineTime(lineNo: Int, time: Timestamp)

  // HandlerのEndTimeは今のところ使用予定なし。また、WindowがCloseした時点でDBオブジェクトを作成して
  // しまうため、HandlerのEndまで記録できない
  private case class Handler(name: String, start: LineTime)
    extends Naming with Starting

  private trait StartupTiming {
    val start: LineTime
    val relatedHandler: Option[Handler]
    val relatedWindow: Option[Window]
    def startupTime: Option[Long] = {
      // relatedHandlerが存在したら、それからstartまで
      // relatedWindowが存在したら、そのwindowのButtonEvent.end、もしくはstartからrelatedWindow.startまで
      // windowにrelatedWindowを代入時、relatedWindowのEndTimeはNoneのため使用できないので要注意
      relatedHandler.map(start.time.getTime - _.start.time.getTime)
        .orElse(for {ww <- relatedWindow
                     ev <- ww.relatedButtonEvent
                     }
            yield ev.end.map(_.time.getTime).getOrElse(ev.start.time.getTime) - ww.start.time.getTime)
    }
  }

  private case class Window(name: String, start: LineTime, underlyingClass: String,
                            end: Option[LineTime] = None,
                            relatedHandler: Option[Handler] = None,
                            relatedButtonEvent: Option[ButtonEvent] = None,
                            relatedWindow: Option[Window] = None)
    extends Naming with Starting with Ending with StartupTiming {

    def toWindowDetail: WindowDetail = {
      WindowDetail(
        logId = 0L,
        lineNo = start.lineNo,
        activator = relatedHandler.map(_.name).orElse(relatedWindow.map(_.name)),
        windowName = Some(name),
        destinationType = None,
        action = relatedButtonEvent.map(_.event),
        method = None,
        time = start.time,
        startupTime = startupTime
      )
    }
  }

  private case class Action(name: String, start: LineTime, end: Option[LineTime] = None,
                            relatedHandler: Option[Handler] = None,
                            relatedButtonEvent: Option[ButtonEvent] = None,
                            relatedWindow: Option[Window] = None)
    extends Naming with Starting with Ending with StartupTiming

  private case class ButtonEvent(name: String, start: LineTime, event: String,
                                 end: Option[LineTime] = None) extends Naming with Starting with Ending

  private val handlerBuffer = ListBuffer[Handler]()
  private val windowBuffer = ListBuffer[Window]()
  private val buttonEventBuffer = ListBuffer[ButtonEvent]()
  private val lineRegex = """([0-9]{4}-[0-9]{2}-[0-9]{2}\s[0-9]{2}:[0-9]{2}:[0-9]{2}\.[0-9]{3})\s\[(.*)\]\[(.*)\](.*)\[(.*)\]\[(j.c.*)\]$""".r
  private val windowNameRegex = """\[(.*)\].*""".r
  private val buttonActionRegex = """.*\((.*)\).*""".r



  private case class FileInfo(appName: String, env: String, computer: String, userId: String, startTime: String) {
    val tradeDate: String = startTime.take(8)
  }

  private case class LineInfo(datetimeStr: String, logLevel: String, appName: String, message: String,
                              thread: String, clazz: String) {
    val datetime = new Timestamp(LineInfo.format.parse(datetimeStr).getTime)
    val underlyingClass: String = getLastAndDelNo(clazz)

    def getLastAndDelNo(name: String): String = {
      val lastName = name.split("\\.").last
      lastName match {
        case LineInfo.lastAndDelNoRegex(n) => n
        case _ => lastName
      }
    }
  }

  private object LineInfo {
    val format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
    val lastAndDelNoRegex: Regex = """(.*)\$[0-9]""".r
  }

  // 2019-10-10 15:54:17.458 [OMS:INFO ][TradeSheet][New Split - Parent Order]Dialog opend.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]
  // datetimeStr             logLevel   appName     message                                th    clazz
  private def getLineInfo(line: String): Option[LineInfo] = {
    line match {
      case lineRegex(datetimeStr, logLevel, appName, message, thread, clazz) => Some(LineInfo(datetimeStr, logLevel, appName, message, thread, clazz))
      case _ => None
    }
  }

  /*
      [TradeSheet][Select Symbol Multi]Dialog opened.[main][j.c.n.n.o.r.p.d.SelectMultiDialog] => Select Symbol Multi
      [TradeSheet]Opened.[main][j.c.n.n.o.r.p.d.c.QuestionDialog] => j.c.n.n.o.r.p.d.c.QuestionDialog
   */
  private def getWindowName(message: String, clazz: String): String = {
    message match {
      case windowNameRegex(windowName) => windowName
      case _ => clazz
    }
  }

  private def getButtonEvent(message: String): Option[String] = {
    regexOption(buttonActionRegex, message)
  }

  private def regexOption(regex: Regex, message: String): Option[String] = {
    message match {
      case regex(contents) => Some(contents)
      case _ => None
    }
  }



  private def findRelatedHandler(handlerOp: Option[Handler], underlyingClass: String)
                            (implicit config: Config): Option[Handler] = {
    try {
      val values = config.getStringList("HandlerMapping" + "." + underlyingClass).asScala
      handlerOp.flatMap { handler =>
        if (values.contains(handler.name)) handlerOp else None
      }
    } catch { case _ :ConfigException.Missing => None }
  }

  private def findRelatedWindow(windowOp: Option[Window], underlyingClass: String)
                               (implicit config: Config): Option[Window] = {
    try {
      val values = config.getStringList("WindowMapping" + "." + underlyingClass).asScala
      windowOp.flatMap { window =>
        if (values.contains(window.name)) windowOp else None
      }
    } catch { case _ :ConfigException.Missing => None }
  }

  /** 同じ名前でEndがまだ登録されていないオブジェクトをBufferから見つけ出し、Endを更新します。
    * fileNameが指定されてかつ対象が見つからなかった場合、Warningを出力します。
    *
    *  @param  name   オブジェクトの名前
    *  @param  buffer 検索対象オブジェクトが格納されているListBuffer
    *  @param  fileName ファイル名(デフォルトnull)
    *  @param  lineNo   行番号(デフォルト0)
    *  @param  f      見つかった検索対象オブジェクトを元に新しいオブジェクトを作成するファンクション
    *  @tparam T         ListBufferの型。NamingとEndingをミックスインしたもの
    */
  private def updateWithEnd[T <: Naming with Ending](name: String, buffer: ListBuffer[T],
                                                     fileName: String = null, lineNo: Int = 0)(f: T => T): Unit = {
    buffer.zipWithIndex.reverseIterator.find {
      case (t, _) => t.end.isEmpty && t.name == name
    } match {
      case Some((t, index)) =>
        buffer.update(index, f(t))
      case None =>
        logger.warn(s"$fileName:$lineNo Couldn't find window from ListBuffer.")
    }
  }

  /** WindowBufferの最後のオブジェクトにイベントを結びつけます。
    * WindowBufferが空の場合、WindowのEndが設定されていない場合、ButtonEventが空の場合、
    * WindowとButtonEventの名前が一致していない場合、
    * WindowEndの行番号よりButtonEventEndもしくはStartの行番号が大きい場合、結びつけは行われません。
    *  @param fileName      ファイル名
    *  @param lineNo        行番号
    *  @param windowBuffer Windowが格納されているListBuffer
    *  @param eventOp       結び付けたいボタンイベント
    */
  private def bindWithButtonEvent(fileName: String, lineNo: Int, windowBuffer: ListBuffer[Window],
                                    eventOp: Option[ButtonEvent]): Unit = {
    val windowOp = windowBuffer.lastOption
    for {
      window <- windowOp
      windowEnd <- window.end
      event <- eventOp
      eventEndOrStart = event.end.getOrElse(event.start)
      if window.name == event.name
      if windowEnd.lineNo > eventEndOrStart.lineNo
    }
    {
      windowBuffer.update(windowBuffer.length - 1, window.copy(relatedButtonEvent = eventOp))
      return
    }
    logger.warn(s"$fileName:$lineNo Couldn't bind Button Event with window.")
  }

  def execute(paths: List[Path]): Unit = {
    paths.foreach(p => execute(p))
  }

  def execute(path: Path): Unit = {

    import jp.co.nri.nefs.tool.log.common.utils.RichFiles.stringToRichString
    import Utils._
    if (isZipFile(path)) {
      val expandedDir = unzip(path)
      val paths = FileUtils.autoClose(Files.list(expandedDir)) { stream =>
        stream.iterator().asScala.toList
      }
      execute(paths)
      delete(expandedDir)
    } else {
      val fileInfo =
        getOMSAplInfo(path.getFileName.toString) match {
          case Some(f) => f
          case None =>
            logger.info("$path was not valid format, so skipped analyzing.")
            return
        }
      val analysisReporter = Log2Case.analysisReporterFactory.create(fileInfo.fileName)
      val outpath = outputdir.resolve(path.getFileName.toString.basename + "Detail" + Keywords.OBJ_EXTENSION)
      val objectOutputStream = new ObjectOutputStream(Files.newOutputStream(outpath))

      val stream = Files.lines(path, Charset.forName("MS932"))
      val lines = stream.iterator().asScala
      try {
        for ((line, tmpNo) <- lines.zipWithIndex; lineNo = tmpNo + 1; lineInfo <- getLineInfo(line)) {
          if (lineInfo.message contains "Handler start.") {
            handlerBuffer += Handler(lineInfo.underlyingClass, LineTime(lineNo, lineInfo.datetime))
          } else if (lineInfo.message contains "Handler end.") {
            // Handler endは使用予定なし
          }
          //[New Basket]Dialog opened.[main][j.c.n.n.o.r.p.d.b.NewBasketDialog$1]
          //[TradeSheet]Opened.[main][j.c.n.n.o.r.p.d.c.QuestionDialog]
          else if ((lineInfo.message contains "Dialog opened.") || (lineInfo.message contains "Opened.")) {
            // 2020/01/03 やはり将来的なリアルタイム分析を有効にするため、この時点でHandlerを検索してしまう。
            val relatedHandler = findRelatedHandler(handlerBuffer.lastOption, lineInfo.underlyingClass)
            val relatedWindow = if (relatedHandler.isDefined) None else {
              findRelatedWindow(windowBuffer.lastOption, lineInfo.underlyingClass)
            }
            windowBuffer += Window(
              name = getWindowName(lineInfo.message, lineInfo.underlyingClass),
              start = LineTime(lineNo, lineInfo.datetime),
              underlyingClass = lineInfo.underlyingClass,
              relatedHandler = relatedHandler,
              relatedWindow = relatedWindow,
            )
          }
          else if ((lineInfo.message contains "Dialog closed.") || (lineInfo.message contains "Closed.")) {
            val windowName = getWindowName(lineInfo.message, lineInfo.underlyingClass)
            updateWithEnd(windowName, windowBuffer,
              fileInfo.fileName, lineNo) {
              window => window.copy(end = Some(LineTime(lineNo, lineInfo.datetime)))
            }
            bindWithButtonEvent(fileInfo.fileName, lineNo, windowBuffer, buttonEventBuffer.lastOption)
            windowBuffer.reverseIterator.find { w => w.name == windowName } match {
              case Some(window) => analysisReporter.report(window.toWindowDetail)
              case None => logger.warn(s"${fileInfo.fileName}:$lineNo Couldn't find window.")
            }
          }
          else if ((lineInfo.message contains "Button event starts") || (lineInfo.message contains "Button Pressed")) {
            getButtonEvent(lineInfo.message) match {
              case Some(event) =>
                buttonEventBuffer += ButtonEvent(
                  name = getWindowName(lineInfo.message, lineInfo.underlyingClass),
                  start = LineTime(lineNo, lineInfo.datetime),
                  event = event
                )
              case None => logger.warn(s"${fileInfo.fileName}:$lineNo Couldn't find action from message.")
            }
          }
          else if (lineInfo.message contains "Button event ends") {
            updateWithEnd(getWindowName(lineInfo.message, lineInfo.underlyingClass), buttonEventBuffer) {
              event => event.copy(end = Some(LineTime(lineNo, lineInfo.datetime)))
            }
          }
        }
      } finally {
        stream.close()
        objectOutputStream.close()
      }


      val istream = new ObjectInputStream(Files.newInputStream(outpath))
      using(istream) { is =>
        Iterator.continually(is.readObject()).takeWhile(_ != null).foreach(v => logger.info(v.toString))
      }
    }
  }
}

object Keywords {
  val OBJ_EXTENSION = ".obj"
  val LOG_SUFFIX = "_Log"
  val WINDOW_DETAIL_SUFFIX = "_WindowDetail"
}

object Utils {
  import Keywords._
  def getObjFile(name: String, suffix: String): String = {
    getBase(name) + suffix + OBJ_EXTENSION
  }
  def getBase(name: String): String = {
    val index = name.lastIndexOf('.')
    if (index != -1)
      name.substring(0, index)
    else
      name
  }

  //java.io.EOFExceptionが出るが無視する
 def using[A <: java.io.Closeable](s: A)(f: A => Unit): Unit = {
    try { f(s) } catch {case _: Exception => } finally { s.close() }
 }


}


object Log2Case extends AnalysisReporterComponent {
  type OptionMap = Map[Symbol, String]
  val usage = """
        Usage: jp.co.nri.nefs.tool.log.analysis.Log2Case [--searchdir dir | --file file | --excelFile file] --outputdir dir
        """
  val analysisReporterFactory = new AnalysisReporterFactory

  sealed trait EExecutionType
  case object LOGDIR extends EExecutionType
  case object LOGFILE extends EExecutionType
  case object EXCELFILE extends EExecutionType



  def main(args: Array[String]): Unit = {

    //lazy val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).log$""".r
    val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).(log|zip)$""".r
    val options = nextOption(Map(), args.toList)
    val (executionType, dirOrFile, outputDir) = getOption(options)
    analysisReporterFactory.setOutputDir(outputDir)
    val log2case = new Log2Case(outputDir)
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
        val excel2Case = new Excel2Case(outputDir)
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

class Excel2Case(outputDir: Path) {
  import Utils._
  import jp.co.nri.nefs.tool.log.common.utils.RichCell.cellToRichCell

  private def collectSheets(book: Workbook, keyword: String): Seq[Sheet] = {
    for {
      no <- 0 until book.getNumberOfSheets
      name = book.getSheetName(no)
      if name.contains(keyword)
      sheet = book.getSheetAt(no)
    } yield sheet
  }

  private def writeObj[T](path: Path, sheet: Sheet)
                      (f: Sheet => Seq[T]): Unit = {
    import Keywords.OBJ_EXTENSION
    val objs = f(sheet)
    val outpath = outputDir.resolve(sheet.getSheetName + OBJ_EXTENSION)
    val ostream = new ObjectOutputStream(Files.newOutputStream(outpath))
    // 書き込み
    using(ostream) { os =>
      objs.foreach(l =>
        os.writeObject(l)
      )
    }
    // 念のため読み込みなおしてログ出力
    val istream = new ObjectInputStream(Files.newInputStream(outpath))
    using(istream) { is =>
      Iterator.continually(is.readObject()).takeWhile(_ != null).foreach(v => println(v))
    }
  }

  def execute(path: Path): Unit = {
    import Keywords._
    val book = WorkbookFactory.create(path.toFile)
    val logSheets = collectSheets(book, LOG_SUFFIX)
    val detailSheets = collectSheets(book, WINDOW_DETAIL_SUFFIX)

    for {sheet <- logSheets} {
      writeObj(path, sheet){ s =>
        val logs = for {
          (row, rownum) <- s.iterator().asScala.zipWithIndex
          if rownum > 0 //先頭行スキップ
          iterator = row.iterator()
          logId = 0L
          appName = iterator.next().getValue[String].get
          computerName = iterator.next().getValue[String].get
          userId = iterator.next().getValue[String].get
          tradeDate = iterator.next().getValue[String].get
          time = iterator.next().getValue[Timestamp].get
        } yield Log(logId, appName, computerName, userId, tradeDate, time)
        logs.toSeq
      }
    }

    for {sheet <- detailSheets}{
      writeObj(path, sheet){ s =>
        (for {
          (row, rownum) <- s.iterator().asScala.zipWithIndex
          if rownum > 0 //先頭行スキップ
          iterator = row.iterator()
          lineNo = iterator.next().getValue[Int].get
          handler = iterator.next().getValue[String]
          windowName = iterator.next().getValue[String]
          destinationType = iterator.next().getValue[String]
          action = iterator.next().getValue[String]
          method = iterator.next().getValue[String]
          time = iterator.next().getValue[Timestamp].get
          startupTime = iterator.next().getValue[Long]
          detail = WindowDetail(0L, lineNo,
            handler, windowName, destinationType, action, method, time, startupTime)
        } yield detail).toSeq
      }
    }
  }
}