package jp.co.nri.nefs.tool.log.analysis

import java.sql.Timestamp

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.log.common.model.WindowDetail

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.util.matching.Regex


trait LogAnalyzerFactoryComponent {
  self: AnalysisWriterComponent =>
  val logAnalyzerFactory: LogAnalyzerFactory

  trait LogAnalyzerFactory {
    def create(fileName: String): LogAnalyzer
  }

  class DefaultLogAnalyzerFactory extends LogAnalyzerFactory {
    def create(fileName: String): LogAnalyzer = {
      val analysisWriter = analysisWriterFactory.create(fileName)
      new DefaultLogAnalyzer(fileName, analysisWriter)
    }
  }

  trait LogAnalyzer {
    def analyze(line: String, lineNo: Int): Unit
  }

  protected trait Naming {
    val name: String
  }

  protected trait Starting {
    val start: LineTime
  }

  protected trait Ending {
    val end: Option[LineTime]
  }

  protected case class LineTime(lineNo: Int, time: Timestamp)

  // HandlerのEndTimeは今のところ使用予定なし。また、WindowがCloseした時点でDBオブジェクトを作成して
  // しまうため、HandlerのEndまで記録できない
  protected case class Handler(name: String, start: LineTime)
    extends Naming with Starting

  protected trait StartupTiming {
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

  protected case class LineInfo(datetimeStr: String, service: String, logLevel: String, appName: String, message: String,
                                thread: String, clazz: String) {
    val datetime = new Timestamp(LineInfo.format.parse(datetimeStr).getTime)
    val underlyingClass: String = getLastAndDelNo(clazz)
    val windowName: String = getWindowName(message, underlyingClass)
    val buttonEvent: Option[String] = getButtonEvent(message)

    /** "."で区切られた最後の文字列から$数字を取り除いた文字列を返します。
      *
      * @param name 対象文字列
      * @return 変換後の文字列
      */
    def getLastAndDelNo(name: String): String = {
      val lastName = name.split("\\.").last
      lastName match {
        case LineInfo.lastAndDelNoRegex(n) => n
        case _ => lastName
      }
    }

    /** メッセージに含まれる括弧内の文字列を抜き出します。
      * 例1: ("[Select Symbol Multi]Dialog opened.", "SelectMultiDialog") => "Select Symbol Multi"
      * 例2: ("Opened.", "QuestionDialog") => "QuestionDialog"
      *
      * @param  message メッセージ
      * @param  default 括弧内の文字列がなかった場合に算出される文字列
      * @return 括弧内の文字列もしくは引数で指定されたデフォルト値
      */
    private def getWindowName(message: String, default: String): String = {
      message match {
        case LineInfo.windowNameRegex(n) => n
        case _ => clazz
      }
    }

    /** メッセージに含まれる丸括弧内の文字列を抜き出します。
      */
    private def getButtonEvent(message: String): Option[String] = {
      message match {
        case LineInfo.buttonActionRegex(e) => Some(e)
        case _ => None
      }
    }
  }

  protected object LineInfo {
    private val config: Config = ConfigFactory.load()
    private val timeExpression: String = """([0-9]{4}-[0-9]{2}-[0-9]{2}\s[0-9]{2}:[0-9]{2}:[0-9]{2}\.[0-9]{3})""" + """\s"""
    private val serviceNames: List[String] = config.getStringList("serviceNames").asScala.toList
    private val serviceExpression: String = """\[(""" + serviceNames.mkString("|") + """)""" + ":"
    private val levels: List[String] = config.getStringList("levels").asScala.toList
    private val logLevelExpression: String = """(""" + levels.mkString("|") + """) \]"""
    private val applications: List[String] = config.getStringList("applications").asScala.toList
    private val applicationExpression: String = """\[(""" + applications.mkString("|") + """)\]"""
    private val messageExpression: String = """(.*)"""
    private val threadExpression: String = """\[(.*)\]"""
    private val classExpression: String = """\[(j.c.*)\]$"""
    private val lineRegex = new Regex(timeExpression + serviceExpression + logLevelExpression + applicationExpression +
      messageExpression + threadExpression + classExpression)
    private val windowNameRegex = """\[(.*)\].*""".r
    private val buttonActionRegex = """.*\((.*)\).*""".r
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


  class DefaultLogAnalyzer(fileName: String, analysisWriter: AnalysisWriter) extends LogAnalyzer with LazyLogging {

    protected val handlerBuffer: ListBuffer[Handler] = ListBuffer[Handler]()
    protected val windowBuffer: ListBuffer[Window] = ListBuffer[Window]()
    protected val buttonEventBuffer: ListBuffer[ButtonEvent] = ListBuffer[ButtonEvent]()

    def analyze(line: String, lineNo: Int): Unit = {
      val lineInfo = LineInfo.valueOf(line) match {
        case Some(info) => info
        case None => return
      }

      if (lineInfo.message contains "Handler start.") {
        handlerBuffer += Handler(lineInfo.underlyingClass, LineTime(lineNo, lineInfo.datetime))
      } else if (lineInfo.message contains "Handler end.") {
        // Handler endは使用予定なし
      }
      //[New Basket]Dialog opened.[main][j.c.n.n.o.r.p.d.b.NewBasketDialog$1]
      //[TradeSheet]Opened.[main][j.c.n.n.o.r.p.d.c.QuestionDialog]
      else if ((lineInfo.message contains "Dialog opened.") || (lineInfo.message contains "Opened.")) {
        // 将来的なリアルタイム分析を有効にするため、この時点でHandlerを検索してしまう。
        val relatedHandler = findRelatedHandler(handlerBuffer.lastOption, lineInfo.underlyingClass)
        val relatedWindow = if (relatedHandler.isDefined) None else {
          findRelatedWindow(windowBuffer.lastOption, lineInfo.underlyingClass)
        }
        windowBuffer += Window(
          name = lineInfo.windowName,
          start = LineTime(lineNo, lineInfo.datetime),
          underlyingClass = lineInfo.underlyingClass,
          relatedHandler = relatedHandler,
          relatedWindow = relatedWindow,
        )
      }
      else if ((lineInfo.message contains "Dialog closed.") || (lineInfo.message contains "Closed.")) {
        val windowName = lineInfo.windowName
        updateWithEnd(windowName, windowBuffer, fileName, lineNo) {
          window => window.copy(end = Some(LineTime(lineNo, lineInfo.datetime)))
        }
        bindWithButtonEvent(fileName, lineNo, windowBuffer, buttonEventBuffer.lastOption)
        windowBuffer.reverseIterator.find { w => w.name == windowName } match {
          case Some(window) => analysisWriter.write(window.toWindowDetail)
          case None => logger.warn(s"$fileName:$lineNo Couldn't find window.")
        }
      }
      else if ((lineInfo.message contains "Button event starts") || (lineInfo.message contains "Button Pressed")) {
        lineInfo.buttonEvent match {
          case Some(event) =>
            buttonEventBuffer += ButtonEvent(
              name = lineInfo.windowName,
              start = LineTime(lineNo, lineInfo.datetime),
              event = event
            )
          case None => logger.warn(s"$fileName:$lineNo Couldn't find action from message.")
        }
      }
      else if (lineInfo.message contains "Button event ends") {
        updateWithEnd(lineInfo.windowName, buttonEventBuffer) {
          event => event.copy(end = Some(LineTime(lineNo, lineInfo.datetime)))
        }
      }
    }

    /** Window起動時のログに含まれるクラス名が直近のHandlerと紐づく場合、そのHandlerを返します。
      * 紐づかない場合は、Noneを返します。
      * マッピングはあらかじめコンフィグファイルで定義しておく必要があります。
      *
      * @param  handlerOp       直近のHandler
      * @param  underlyingClass Window起動時のログに含まれるクラス名
      * @return 直近のHandler。紐づかない場合はNone
      */
    private def findRelatedHandler(handlerOp: Option[Handler], underlyingClass: String): Option[Handler] = {
      try {
        val config = ConfigFactory.load()
        val values = config.getStringList(ConfigKey.HANDLER_MAPPING + "." + underlyingClass).asScala
        handlerOp.flatMap { handler =>
          if (values.contains(handler.name)) handlerOp else None
        }
      } catch {
        case _: ConfigException.Missing =>
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
        case _: ConfigException.Missing =>
          logger.warn(s"${ConfigKey.WINDOW_MAPPING} was not found in config file")
          None
      }
    }

    /** 同じ名前でEndがまだ登録されていないオブジェクトをBufferから見つけ出し、Endを更新します。
      * fileNameが指定されてかつ対象が見つからなかった場合、Warningを出力します。
      *
      * @param  name     オブジェクトの名前
      * @param  buffer   検索対象オブジェクトが格納されているListBuffer
      * @param  fileName ファイル名(デフォルトnull)
      * @param  lineNo   行番号(デフォルト0)
      * @param  f        見つかった検索対象オブジェクトを元に新しいオブジェクトを作成するファンクション
      * @tparam T ListBufferの型。NamingとEndingをミックスインしたもの
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
      *
      * @param fileName     ファイル名
      * @param lineNo       行番号
      * @param windowBuffer Windowが格納されているListBuffer
      * @param eventOp      結び付けたいボタンイベント
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
      } {
        windowBuffer.update(windowBuffer.length - 1, window.copy(relatedButtonEvent = eventOp))
        return
      }
      logger.warn(s"$fileName:$lineNo Couldn't bind Button Event with window.")
    }
  }

}
