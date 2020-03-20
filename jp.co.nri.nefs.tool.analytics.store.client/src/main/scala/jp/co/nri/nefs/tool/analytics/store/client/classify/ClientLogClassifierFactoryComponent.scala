package jp.co.nri.nefs.tool.analytics.store.client.classify

import java.sql.Timestamp

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.tool.analytics.model.client.{Log, OMSAplInfo, PreCheck, WindowDetail}
import jp.co.nri.nefs.tool.analytics.store.client.record.ClientLogRecorder

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.matching.Regex

trait ClientLogClassifierFactoryComponent {
  val clientLogClassifierFactory: ClientLogClassifierFactory

  trait ClientLogClassifierFactory {
    def create(aplInfo: OMSAplInfo): ClientLogClassifier
  }

  class DefaultClientLogClassifyFactory(clientLogStore: ClientLogRecorder) extends ClientLogClassifierFactory {
    def create(aplInfo: OMSAplInfo): ClientLogClassifier = {
      new DefaultClientLogClassifier(aplInfo, clientLogStore)
    }
  }

  trait ClientLogClassifier {
    def classify(line: String, lineNo: Int): Unit
    def postStop(): Unit
  }

  trait Naming {
    val name: String
  }

  trait Starting {
    val start: LineTime
  }

  trait Ending {
    val end: Option[LineTime]
  }

  case class LineTime(lineNo: Long, time: Timestamp)

  // HandlerのEndTimeは今のところ使用予定なし。また、WindowがCloseした時点でDBオブジェクトを作成して
  // しまうため、HandlerのEndまで記録できない
  case class Handler(name: String, start: LineTime)
    extends Naming with Starting

  trait StartupTiming {
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

  case class Request(property: String) {
    val parameters: ListBuffer[Map[String, String]] = ListBuffer[Map[String, String]]()
    def addParameter(parameter: Map[String, String]): Unit = {
      parameters += parameter
    }
  }

  case class Window(name: String, start: LineTime, underlyingClass: String,
                    end: Option[LineTime] = None,
                    relatedHandler: Option[Handler] = None,
                    relatedButtonEvent: Option[ButtonEvent] = None,
                    relatedWindow: Option[Window] = None,
                    requestBuffer: Option[ListBuffer[Request]] = None)
    extends Naming with Starting with Ending with StartupTiming {

    def toWindowDetail: WindowDetail = {
      WindowDetail(
        logId = 0,
        lineNo = start.lineNo,
        activator = relatedHandler.map(_.name).orElse(relatedWindow.flatMap(w => getWindowName(w.name))),
        windowName = getWindowName(name),
        destinationType = getDestinationType(requestBuffer),
        action = relatedButtonEvent.map(_.event),
        method = None,
        time = start.time,
        startupTime = startupTime
      )
    }

    /** ハイフン以降の文字列は除去、半角全角の空白は除去してwindowNameを取得します。
      * 例1) Order Detail - OO202002130000010561 - 2020/02/13 - => OrderDetail
      * 例2) New Split Cross　　　- Parent Order => NewSplitCross
      */
    private def getWindowName(windowName: String): Option[String] = {
      val index = windowName.indexOf('-')
      val forwardName = if (index > 0) windowName.substring(0, index)
      else windowName
      Some(forwardName.replace(" ", "").replace("　", ""))
    }

    private def getDestinationType(requestBufferOp: Option[ListBuffer[Request]]): Option[String] = {
      (for {
        requestBuffer <- requestBufferOp
        request <- requestBuffer.lastOption
        destinationType = request.property match {
          case "ENewInterventionProperty" => "EXCHANGE"
          case "ENewChildOrderProperty" => "CHILD_ORDER"
          case "ENewChildOrderAndAlgoProperty" | "ENewOrderAndAlgoProperty" => "ALGO"
          case "ENewBlockDetailProperty" =>
            val newBlockRequestOp = requestBuffer.reverseIterator.find(_.property == "ENewBlockProperty")
            val blockType = for {
              newBlockRequest <- newBlockRequestOp
              parameters <- newBlockRequest.parameters.headOption
              bType <- parameters.get("BLOCK_TYPE")
            } yield bType
            blockType.map { bType => if (bType == "WAVE") "WAVE" else "TRADING_LIST" }.getOrElse("UNKNOWN")
          case "ENewBasketCrossProperty" =>
            val dTypeOp = for {
              params <- request.parameters.headOption
              market <- params.get("MARKET")
              crossDestinationType =
              if (market == "TYO_TOST")
                "TOST_PRINCIPAL"
              else if (market == "JSD_OTC")
                "OTC_PRINCIPAL"
              else
                "UNKNOWN"
            } yield crossDestinationType
            dTypeOp.getOrElse("UNKNOWN")
          case "ENewSliceProperty" | "ENewReservedSliceProperty" | "ENewOrderAndSliceProperty" |
               "ENewOrderAndReservedSliceProperty" =>
            val dTypeOp = for {
              params <- request.parameters.headOption
              market <- params.get("MARKET")
              crossCapacity <- params.get("CROSS_CAPACITY")
              crossType <- params.get("CROSS_TYPE")
              crossCounterparty <- params.get("CROSS_COUNTERPARTY")
              crossDestinationType =
              if ((market == "TYO_TOST") && (crossCapacity == "PRINCIPAL"))
                "TOST_PRINCIPAL"
              else if ((market == "TYO_TOST") && (crossCapacity == "AGENCY"))
                "TOST_AGENCY"
              else if ((market == "TYO_TOST") && (crossCapacity == "BROKER"))
                "TOST_BROKER"
              else if ((market == "TYO_TOST") && (crossType == "FIXED_PRICE"))
                "TOST2"
              else if ((market == "TYO_TOST") && (crossType == "BUY_BACK"))
                "TOST3_BUY_BACK"
              else if ((market == "TYO_TOST") && (crossType == "DISTRIBUTION"))
                "TOST3_BUNBAI"
              else if ((market == "JSD_OTC") && (crossCapacity == "PRINCIPAL"))
                "OTC_PRINCIPAL"
              else if ((market == "JSD_OTC") && (crossCapacity == "AGENCY"))
                "OTC_AGENCY"
              else if ((market == "JSD_OTC") && (crossCapacity == "BROKER"))
                "OTC_BROKER"
              else if ((market == "OSA_DERIV") && (crossCapacity == "PRINCIPAL"))
                "JNET_PRINCIPAL"
              else if ((market == "OSA_DERIV") && (crossCapacity == "BROKER"))
                "JNET_BROKER"
              else if (crossType == "FIXED_PRICE")
                "CLOSE_PRICE"
              else if (crossType == "BUY_BACK")
                "BUY_BACK"
              else if (crossType == "DISTRIUBUTION")
                "BUNBAI"
              else if (crossType == "SINGLE")
                "OFF_AUCTION"
              else if (crossCounterparty == "BROKER")
                "ODD_LOT_PRINCIPAL"
              else if (crossCounterparty == "RETELA")
                "ODD_LOT_AGENCY"
              else
                "EXCHANGE"
            } yield crossDestinationType
            dTypeOp.getOrElse("UNKNOWN")
          case _ => "UNKNOWN"
        }
      } yield destinationType).flatMap { d =>
        if (d == "UNKNOWN")
          None
        else
          Some(d)
      }
    }

  }

  case class ButtonEvent(name: String, start: LineTime, event: String,
                         end: Option[LineTime] = None)
    extends Naming with Starting with Ending

  case class LineInfo(datetimeStr: String, service: String, logLevel: String, appName: String, message: String,
                                thread: String, clazz: String) {
    // SimpleDateFormatはスレッドセーフではない
    private val format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
    val datetime = new Timestamp(format.parse(datetimeStr).getTime)
    lazy val underlyingClass: String = getLastAndDelNo(clazz)
    lazy val windowName: String = getWindowName(message, underlyingClass)
    lazy val buttonEvent: Option[String] = getButtonEvent(message)
    lazy val requestProperty: Option[String] = getRequestProperty(message)
    lazy val requestParameter: Option[Map[String, String]] = getRequestParameter(message)
    lazy val preCheck: Option[(String, String)] = getPreCheck(message)

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
      * 例3: ("[]Dialog Opened.", "QuickInputDialog") => "QuickInputDialog"
      *
      * @param  message メッセージ
      * @param  default 括弧内の文字列がなかった場合に算出される文字列
      * @return 括弧内の文字列もしくは引数で指定されたデフォルト値
      */
    private def getWindowName(message: String, default: String): String = {
      getMessageInFirstBrackets(message) match {
        case Some(n) => n
        case _ => default
      }
    }

    private def getMessageInFirstBrackets(message: String): Option[String] = {
      val beginIndex = message.indexOf('[')
      val endIndex = message.indexOf(']')
      if ((beginIndex >= 0) && (endIndex > 0) && (endIndex > beginIndex)) {
        Some(message.substring(beginIndex+1, endIndex))
      } else None
    }

    private def getMessageInLastBrackets(message: String): Option[String] = {
      val beginIndex = message.lastIndexOf('[')
      val endIndex = message.lastIndexOf(']')
      if ((beginIndex >= 0) && (endIndex > 0) && (endIndex > beginIndex)) {
        Some(message.substring(beginIndex+1, endIndex))
      } else None
    }


    /** メッセージに含まれる丸括弧内の文字列を抜き出します。
      */
    private def getButtonEvent(message: String): Option[String] = {
      message match {
        case LineInfo.buttonActionRegex(e) => Some(e)
        case _ => None
      }
    }

    /** メッセージに含まれる鍵括弧内の文字列を抜き出します。
     */
    private def getRequestProperty(message: String): Option[String] = {
      message match {
        case LineInfo.requestRegex(r) => Some(r)
        case _ => None
      }
    }

    /** メッセージに含まれる波括弧内の文字列を抜き出し、key=value形式に分割し、Mapに変換します。
      */
    private def getRequestParameter(message: String): Option[Map[String, String]] = {
      message match {
        case LineInfo.parameterRegex(p) =>
          Some(p.split(",").collect{
            case LineInfo.keyValueRegex(key, value) => (key.trim, value.trim)
          }.toMap)
        case _ => None
      }
    }

    private def getPreCheck(message: String): Option[(String, String)] = {
      val windowNameOp = getMessageInFirstBrackets(message)
      for {
        windowName <- windowNameOp
        message2 = message.replace("[" + windowName + "]", "")
        code <- getMessageInLastBrackets(message2)
        checkMessage = message2.replace("[" + code + "]", "")
      } yield (code, checkMessage)
    }
  }

  object LineInfo {
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
    private val buttonActionRegex = """.*\((.*)\).*""".r
    private val lastAndDelNoRegex = """(.*)\$[0-9]""".r
    private val requestRegex = """REQUEST=\[(.*)\],.*$""".r
    private val parameterRegex = """PARAMETER=DefaultEntity:\{(.*)\}""".r
    private val keyValueRegex = """(.*)=(.*)""".r

    def valueOf(line: String): Option[LineInfo] = {
      line match {
        case lineRegex(datetimeStr, service, logLevel, appName, message, thread, clazz) =>
          Some(LineInfo(datetimeStr, service, logLevel, appName, message, thread, clazz))
        case _ => None
      }
    }
  }

  object KEY_MESSAGE {
    final val HANDLER_START = "Handler start."
    final val HANDLER_END = "Handler end."
  }

  class DefaultClientLogClassifier(aplInfo: OMSAplInfo, clientLogRecorder: ClientLogRecorder) extends ClientLogClassifier with LazyLogging {

    final val HANDLER_MAPPING = "HandlerMapping"
    final val WINDOW_MAPPING = "WindowMapping"
    final val INPUT_DIR = "inputDir"
    final val OUT_DIR = "outDir"
    val handlerBuffer: ListBuffer[Handler] = ListBuffer[Handler]()
    val windowBuffer: ListBuffer[Window] = ListBuffer[Window]()
    val buttonEventBuffer: ListBuffer[ButtonEvent] = ListBuffer[ButtonEvent]()
    val requestBuffer: ListBuffer[Request] = ListBuffer[Request]()
    val futureBuffer: ListBuffer[Future[Int]] = ListBuffer()
    private lazy val logId: Option[Int] = clientLogRecorder.record(
      Log(0, aplInfo.appName, aplInfo.computer,
        aplInfo.userId, aplInfo.tradeDate, aplInfo.time, aplInfo.fileName)
    )

    def postStop(): Unit = {
      logger.info("postStop starts")
      val aggFut = Future.sequence(futureBuffer)
      Await.result(aggFut, Duration.Inf)
      logger.info("postStop ends")
    }

    def classify(line: String, lineNo: Int): Unit = {
      val lineInfo = LineInfo.valueOf(line) match {
        case Some(info) => info
        case None => return
      }

      if (lineInfo.message contains KEY_MESSAGE.HANDLER_START) {
        handlerBuffer += Handler(lineInfo.underlyingClass, LineTime(lineNo, lineInfo.datetime))
      } else if (lineInfo.message contains KEY_MESSAGE.HANDLER_END) {
        // Handler endは使用予定なし
      }
      //[New Basket]Dialog opened.[main][j.c.n.n.o.r.p.d.b.NewBasketDialog$1]
      //[TradeSheet]Opened.[main][j.c.n.n.o.r.p.d.c.QuestionDialog]
      else if ((lineInfo.message contains "Dialog opened.") || (lineInfo.message contains "Opened.")) {
        // 将来的なリアルタイム分析を有効にするため、この時点でHandlerを検索してしまう。
        val relatedHandler = findRelatedHandler(handlerBuffer, lineInfo.underlyingClass)
        val relatedWindow = if (relatedHandler.isDefined) None else {
          findRelatedWindow(windowBuffer, lineInfo.underlyingClass)
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
        updateWithEnd(windowName, windowBuffer, aplInfo.fileName, lineNo) {
          window => window.copy(end = Some(LineTime(lineNo, lineInfo.datetime)))
        }
        bindWithButtonEvent(aplInfo.fileName, lineNo, windowBuffer, windowName, buttonEventBuffer)
        windowBuffer.zipWithIndex.reverseIterator.find { case (w, _) => w.name == windowName } match {
          case Some((window, index)) =>
            if (logId.nonEmpty) {
              futureBuffer += clientLogRecorder.record(logId.get, window.toWindowDetail)
            }
            windowBuffer.remove(index)
          case None => logger.warn(s"$aplInfo.fileName:$lineNo Couldn't find window.")
        }
        // リクエストバッファはクリアしておく
        requestBuffer.clear()
      }
      else if ((lineInfo.message contains "Button event starts") || (lineInfo.message contains "Button Pressed")) {
        lineInfo.buttonEvent match {
          case Some(event) =>
            buttonEventBuffer += ButtonEvent(
              name = lineInfo.windowName,
              start = LineTime(lineNo, lineInfo.datetime),
              event = event
            )
          case None => logger.warn(s"${aplInfo.fileName}:$lineNo Couldn't find action from message.")
        }
      }
      else if (lineInfo.message contains "Button event ends") {
        updateWithEnd(lineInfo.windowName, buttonEventBuffer) {
          event => event.copy(end = Some(LineTime(lineNo, lineInfo.datetime)))
        }
      } else if (lineInfo.underlyingClass == "DefaultValidationDataManager") {
        for ((code, checkMsg) <- lineInfo.preCheck; id <- logId) {
          futureBuffer += clientLogRecorder.record(PreCheck(id, lineNo, Some(lineInfo.windowName), code, checkMsg))
        }
      }
      for (property <- lineInfo.requestProperty) requestBuffer += Request(property)

      for (parameter <- lineInfo.requestParameter; request <- requestBuffer.lastOption) {
        request.addParameter(parameter)
      }
    }

    /** Handlerのリストから直近のHandlerを抜き出し、それがクラス名と紐づく場合、そのHandlerを返します。
      * 紐づかない場合は、Noneを返します。
      * マッピングはあらかじめコンフィグファイルで定義しておく必要があります。
      *
      * @param  handlerBuffer   Handlerが格納されているListBuffer
      * @param  underlyingClass クラス名
      * @return 直近のHandler。紐づかない場合はNone
      */
    private def findRelatedHandler(handlerBuffer: ListBuffer[Handler], underlyingClass: String): Option[Handler] = {
      findRelatedObject[Handler](handlerBuffer, underlyingClass, _.name, HANDLER_MAPPING)
    }

    /** Windowのリストから直近のWindowを抜き出し、それがクラス名と紐づく場合、そのHandlerを返します。
      * 紐づかない場合は、Noneを返します。
      * マッピングはあらかじめコンフィグファイルで定義しておく必要があります。
      *
      * @param  windowBuffer   Handlerが格納されているListBuffer
      * @param  underlyingClass クラス名
      * @return 直近のHandler。紐づかない場合はNone
      */
    private def findRelatedWindow(windowBuffer: ListBuffer[Window], underlyingClass: String): Option[Window] = {
      findRelatedObject[Window](windowBuffer, underlyingClass, _.underlyingClass, WINDOW_MAPPING)
    }

    private def findRelatedObject[T](buffer: ListBuffer[T], underlyingClass: String, f: T => String, configKeyPrefix: String): Option[T] = {
      val config = ConfigFactory.load()
      val configKey = configKeyPrefix + "." + underlyingClass
      try {
        val values = config.getStringList(configKey).asScala
        val lastOption = buffer.lastOption
        lastOption.flatMap { o =>
          if (values.contains(f(o))) lastOption else None
        }
      } catch {
        case e: ConfigException.Missing =>
          logger.warn(s"$configKey was not found in config file",e)
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
      * @param windowName   結び付けたいwindowName
      * @param buttonEventBuffer 結び付けたいボタンイベントが格納されているListBuffer
      */
    private def bindWithButtonEvent(fileName: String, lineNo: Int, windowBuffer: ListBuffer[Window],
                                    windowName: String, buttonEventBuffer: ListBuffer[ButtonEvent]): Unit = {

      val eventOpWithIndex = buttonEventBuffer.zipWithIndex.reverseIterator.find { case (buttonEvent, _) =>
        buttonEvent.name == windowName
      }
      windowBuffer.zipWithIndex.reverseIterator.find { case (window, _) =>
        (for {
          windowEnd <- window.end
          (event, _) <- eventOpWithIndex
          eventEndOrStart = event.end.getOrElse(event.start)
          if window.name == event.name
          if windowEnd.lineNo > eventEndOrStart.lineNo
          result = true
        } yield result).getOrElse(false)
      } match {
        case Some((window, index)) =>
          windowBuffer.update(index, window.copy(relatedButtonEvent = eventOpWithIndex.map(_._1)))
          // bindされたbuttonEventはbufferから削除
          buttonEventBuffer.remove(eventOpWithIndex.map(_._2).get)
        case _ =>
          logger.info(s"$fileName:$lineNo Couldn't bind Button Event with window.")
      }
    }

    override def toString: String = {
      s"${getClass.getSimpleName}(${aplInfo.fileName})"
    }

  }

}
