package jp.co.nri.nefs.tool.log.analysis

import java.io.ObjectOutputStream
import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import java.sql.Timestamp

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.log.common.model.{Log, WindowDetail}
import jp.co.nri.nefs.tool.log.common.utils.FileUtils._
import jp.co.nri.nefs.tool.log.common.utils.RegexUtils._
import jp.co.nri.nefs.tool.log.common.utils.ZipUtils._

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.collection.breakOut

trait ReaderComponent {
  val reader: Reader

  trait Reader {
    def read: Map[String, java.util.stream.Stream[String]]
    def closing(): Unit = {}
  }

  class DefaultReader(implicit val config: Config) extends Reader {
    private val input: Path = Paths.get(config.getString("key"))
    private val deletingBuffer: ListBuffer[Path] = ListBuffer()

    def read: Map[String, java.util.stream.Stream[String]] = {
      val buffer = ListBuffer[Path]()
      pickupPath(buffer, input)
      (for (path <- buffer) yield {
        path.getFileName.toString -> Files.lines(path, Charset.forName("MS932"))
      }) (breakOut)
    }

    // 解凍したディレクトリは削除する
    override def closing(): Unit = {
      deletingBuffer.foreach(delete)
    }

    private def pickupPath(buffer: ListBuffer[Path], input: Path): Unit = {
      if (!Files.isDirectory(input)) {
        if (isZipFile(input)) {
          val expandedDir = unzip(input)
          deletingBuffer += expandedDir
          val files = autoClose(Files.list(expandedDir)) { stream =>
            stream.iterator().asScala.toList
          }
          for (file <- files) pickupPath(buffer, file)
        } else {
          buffer += input
        }
      } else {
        val files = autoClose(Files.list(input)) { stream =>
          stream.iterator().asScala.toList
        }
        for (file <- files) pickupPath(buffer, file)
      }
    }
  }

}


trait AnalysisWriterComponent {
  val analysisWriterFactory: AnalysisWriterFactory

  trait AnalysisWriterFactory {
    def create(fileName: String): AnalysisWriter
  }

  class DefaultAnalysisWriterFactory(implicit val config: Config) extends AnalysisWriterFactory {
    val key = "outDir"
    private val outputDir: Path = Paths.get(config.getString("key"))
    def create(fileName: String): DefaultAnalysisWriter = {
      new DefaultAnalysisWriter(outputDir, fileName)
    }
  }

  trait AnalysisWriter {
    def write(log: Log): Unit
    def write(detail: WindowDetail): Unit
  }

  class DefaultAnalysisWriter(outputDir: Path = null, fileName: String = null) extends AnalysisWriter with LazyLogging {
    import jp.co.nri.nefs.tool.log.common.utils.RichFiles.stringToRichString

    private lazy val outLogPath = outputDir.resolve(fileName.basename + "Log" + Keywords.OBJ_EXTENSION)
    private lazy val logOutputStream = new ObjectOutputStream(Files.newOutputStream(outLogPath))
    private lazy val outDetailPath = outputDir.resolve(fileName.basename + "Detail" + Keywords.OBJ_EXTENSION)
    private lazy val detailOutputStream = new ObjectOutputStream(Files.newOutputStream(outDetailPath))

    def write(log: Log): Unit = {
      doWrite(log, logOutputStream)
    }

    def write(detail: WindowDetail): Unit = {
      doWrite(detail, detailOutputStream)
    }

    private def doWrite[T](obj: T, stream: ObjectOutputStream): Unit = {
      autoClose(stream){ s =>
        try {
          logger.info(obj.toString)
          s.writeObject(obj)
        } catch { case _: Exception => }
      }
    }

  }
}

trait LogAnalysisService extends LazyLogging { this: ReaderComponent with AnalysisWriterComponent =>

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

  protected case class Window(name: String, start: LineTime, underlyingClass: String,
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

  protected case class Action(name: String, start: LineTime, end: Option[LineTime] = None,
                            relatedHandler: Option[Handler] = None,
                            relatedButtonEvent: Option[ButtonEvent] = None,
                            relatedWindow: Option[Window] = None)
    extends Naming with Starting with Ending with StartupTiming

  protected case class ButtonEvent(name: String, start: LineTime, event: String,
                                 end: Option[LineTime] = None) extends Naming with Starting with Ending

  protected val handlerBuffer: ListBuffer[Handler] = ListBuffer[Handler]()
  protected val windowBuffer: ListBuffer[Window] = ListBuffer[Window]()
  protected val buttonEventBuffer: ListBuffer[ButtonEvent] = ListBuffer[ButtonEvent]()
  private val windowNameRegex = """\[(.*)\].*""".r
  private val buttonActionRegex = """.*\((.*)\).*""".r



  private case class FileInfo(appName: String, env: String, computer: String, userId: String, startTime: String) {
    val tradeDate: String = startTime.take(8)
  }

  protected case class LineInfo(datetimeStr: String, service: String, logLevel: String, appName: String, message: String,
                              thread: String, clazz: String) {
    val datetime = new Timestamp(LineInfo.format.parse(datetimeStr).getTime)
    val underlyingClass: String = getLastAndDelNo(clazz)

    /** "."で区切られた最後の文字列から$数字を取り除いた文字列を返します。
     * @param name 対象文字列
     * @return     変換後の文字列
     */
    def getLastAndDelNo(name: String): String = {
      val lastName = name.split("\\.").last
      lastName match {
        case LineInfo.lastAndDelNoRegex(n) => n
        case _ => lastName
      }
    }
  }

  protected object LineInfo {
    private val config: Config = ConfigFactory.load()
    private val timeExpression: String = """([0-9]{4}-[0-9]{2}-[0-9]{2}\s[0-9]{2}:[0-9]{2}:[0-9]{2}\.[0-9]{3})""" + """\s"""
    private val serviceNames: List[String] = config.getStringList("serviceNames").asScala.toList
    private val serviceExpression: String  = """\[(""" + serviceNames.mkString("|") + """)""" + ":"
    private val levels: List[String] = config.getStringList("levels").asScala.toList
    private val logLevelExpression: String =  """(""" + levels.mkString("|") + """) \]"""
    private val applications: List[String] = config.getStringList("applications").asScala.toList
    private val applicationExpression: String = """\[(""" + applications.mkString("|") + """)\]"""
    private val messageExpression: String = """(.*)"""
    private val threadExpression: String = """\[(.*)\]"""
    private val classExpression: String = """\[(j.c.*)\]$"""
    private val lineRegex = new Regex(timeExpression + serviceExpression + logLevelExpression + applicationExpression +
      messageExpression + threadExpression + classExpression)

    private val format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
    private val lastAndDelNoRegex: Regex = """(.*)\$[0-9]""".r

    def valueOf(line: String): Option[LineInfo] = {
      line match {
        case lineRegex(datetimeStr, service, logLevel, appName, message, thread, clazz) =>
          Some(LineInfo(datetimeStr, service, logLevel, appName, message, thread, clazz))
        case _ => None
      }
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

  /** Window起動時のログに含まれるクラス名が直近のHandlerと紐づく場合、そのHandlerを返します。
    * 紐づかない場合は、Noneを返します。
    * マッピングはあらかじめコンフィグファイルで定義しておく必要があります。
    * @param  handlerOp        直近のHandler
    * @param  underlyingClass Window起動時のログに含まれるクラス名
    * @return                   直近のHandler。紐づかない場合はNone
    */
  private def findRelatedHandler(handlerOp: Option[Handler], underlyingClass: String): Option[Handler] = {
    try {
      val config = ConfigFactory.load()
      val values = config.getStringList(ConfigKey.HANDLER_MAPPING + "." + underlyingClass).asScala
      handlerOp.flatMap { handler =>
        if (values.contains(handler.name)) handlerOp else None
      }
    } catch {
      case _ :ConfigException.Missing =>
        logger.warn(s"${ConfigKey.HANDLER_MAPPING} was not found in config file")
        None
    }
  }

  private def findRelatedWindow(windowOp: Option[Window], underlyingClass: String): Option[Window] = {
    try {
      val config = ConfigFactory.load()
      val values = config.getStringList(ConfigKey.WINDOW_MAPPING + "." + underlyingClass).asScala
      windowOp.flatMap { window =>
        if (values.contains(window.name)) windowOp else None
      }
    } catch {
      case _ :ConfigException.Missing =>
        logger.warn(s"${ConfigKey.WINDOW_MAPPING} was not found in config file")
        None
    }
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

  /*
  for ((fileName, stream) <- reader.read) yield {
	Future{
		analyze()
	}
}

   */

  def analyze(): Unit = {
    reader.read.foreach {case (fileName, stream) => try {
      val fileInfo =
        getOMSAplInfo(fileName) match {
          case Some(f) => f
          case None =>
            logger.info("$path was not valid format, so skipped analyzing.")
            return
        }
      val analysisWriter = analysisWriterFactory.create(fileInfo.fileName)
      val lines = stream.iterator().asScala

        for ((line, tmpNo) <- lines.zipWithIndex; lineNo = tmpNo + 1; lineInfo <- LineInfo.valueOf(line)) {
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
              case Some(window) => analysisWriter.write(window.toWindowDetail)
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
        reader.closing()
      }
    }
  }
}

object Keywords {
  val OBJ_EXTENSION = ".obj"
  val LOG_SUFFIX = "_Log"
  val WINDOW_DETAIL_SUFFIX = "_WindowDetail"
}

object ConfigKey {
  val HANDLER_MAPPING = "HandlerMapping"
  val WINDOW_MAPPING = "WindowMapping"
  val TRGFILE_NAME = "trgFileName"
  val TRGFILE_PATTERN = "trgFilePattern"
  val TRGFILE_DELIMITER = "trgFileDelimiter"
  val TRGFILE_LAD = "trgFileLAD"
  val TARGETFILE_BASE = "targetFileBase"
  val SLEEP_SECONDS = "sleepSeconds"
  val MASKED_KEY_WORDS = "maskedKeyWords"
  val AWAIT_LIMIT_MINUTES = "awaitLimitMinutes"
  val ZIP_CMD = "zipCmd"
}


object LogAnalyzer extends
  LogAnalysisService with
  ReaderComponent with AnalysisWriterComponent {
  implicit val config: Config = ConfigFactory.load()
  val reader = new DefaultReader
  val analysisWriterFactory = new DefaultAnalysisWriterFactory

  def main(args: Array[String]): Unit = {
    analyze()
  }
}
