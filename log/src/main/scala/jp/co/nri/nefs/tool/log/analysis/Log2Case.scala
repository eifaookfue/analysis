package jp.co.nri.nefs.tool.log.analysis

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}
import java.sql.Timestamp
import java.util.Date

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
import scala.reflect.ClassTag
import scala.util.matching.Regex


class Log2Case(outputdir: Path) extends LazyLogging{

  private trait Naming {def name: String}
  private trait Starting {def start: LineTime}
  private trait Ending {def end: Option[LineTime]}
  private case class LineTime(lineNo: Int, time: Date)
  private case class Handler(name: String, start: LineTime, end: Option[LineTime] = None) extends Naming with Ending
  private case class Window(name: String, start: LineTime, end: Option[LineTime] = None,
                            activateStart: Option[LineTime] = None, activateEnd: Option[LineTime] = None) extends Naming with Ending
  private case class ButtonEvent(event: String, point: LineTime)

  /**
    * キー：windowName
    * バリュー：Windowクラスのリスト。追加する必要があるためmutableなListBufferを用いる
    */
  private val windowDetailMap = mutable.Map[Option[String], ListBuffer[WindowDetail]]()
  private val handlerBuffer = ListBuffer[Handler]()
  private val windowBuffer = ListBuffer[Window]()
  private val buttonEventBuffer = ListBuffer[ButtonEvent]()
  private val lineRegex = """([0-9]{4}-[0-9]{2}-[0-9]{2}\s[0-9]{2}:[0-9]{2}:[0-9]{2}\.[0-9]{3})\s\[(.*)\]\[(.*)\](.*)\[(.*)\]\[(j.c.*)\]$""".r
  private val windowNameRegex = """\[(.*)\].*""".r
  private val buttonActionRegex = """.*\((.*)\).*""".r
  private val lastAndDelNoRegex = """(.*)\$[0-9]""".r

  private case class FileInfo(appName: String, env: String, computer: String, userId: String, startTime: String){
    val tradeDate: String = startTime.take(8)
  }

  private case class LineInfo(datetimeStr: String, logLevel: String, appName: String, message: String,
                              thread: String, clazz: String){
    lazy val format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
    val datetime = new Timestamp(format.parse(datetimeStr).getTime)
  }

  // 2019-10-10 15:54:17.458 [OMS:INFO ][TradeSheet][New Split - Parent Order]Dialog opend.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]
  // datetimeStr             logLevel   appName     message                                th    clazz
  private def getLineInfo(line: String): Option[LineInfo] = {
    line match {
      case lineRegex(datetimeStr, logLevel, appName, message, thread, clazz) => Some(LineInfo(datetimeStr, logLevel, appName, message, thread, clazz))
      case _ => None
    }
  }

  // [New Split - Parent Order]
  //
  private def getWindowName(message: String, clazz: String): Option[String] = {
    regexOption(windowNameRegex, message).orElse(Some(clazz))
  }

  private def getButtonAction(message: String) : Option[String] = {
    regexOption(buttonActionRegex, message)
  }

  private def regexOption(regex: Regex, message: String):Option[String] = {
    message match {
      case regex(contents) => Some(contents)
      case _ => None
    }
  }

  /*
  j.c.n.n.o.r.p.d.AmendOrderSingleDialog$1 => AmendOrderSingleDialogに変換
   */
  private def getLastAndDelNo(name: String): String = {
    val lastName = name.split("\\.").last
    lastName match {
      case lastAndDelNoRegex(n) => n
      case _ => lastName
    }
  }

  def execute(paths: List[Path]): Unit = {
    paths.foreach(p => execute(p))
  }

  def execute(path: Path): Unit = {

    import Utils._
    if (isZipFile(path)){
      val expandedDir = unzip(path)
      val paths = FileUtils.autoClose(Files.list(expandedDir)){ stream =>
        stream.iterator().asScala.toList
      }
      execute(paths)
      delete(expandedDir)
    } else {
      val fileInfo =
        getOMSAplInfo(path.getFileName.toString) match {
          case Some(f) => f
          case None =>
            logger.info("not valid format")
            return
        }

      val stream = Files.lines(path, Charset.forName("MS932"))
      val lines = stream.iterator().asScala
      for ( (line, tmpNo) <- lines.zipWithIndex; lineNo = tmpNo + 1; lineInfo <- getLineInfo(line)) {
        if (lineInfo.message contains "Handler start.") {
          handlerBuffer += Handler(lineInfo.clazz, LineTime(lineNo, lineInfo.datetime))
        } else if (lineInfo.message contains "Handler end."){
          handlerBuffer.zipWithIndex.reverseIterator
            .find { case (h, _) => h.end.isEmpty && h.name.equals(lineInfo.clazz)
            } match {
            case Some((handler, index)) =>
              handlerBuffer.update(index, handler.copy(end = Some(LineTime(lineNo, lineInfo.datetime))))
            case _ => logger.warn(s"${fileInfo.fileName}:$lineNo Couldn't find handler from message.")
          }
        }
        //[New Basket]Dialog opened.[main][j.c.n.n.o.r.p.d.b.NewBasketDialog$1]
        //[TradeSheet]Opened.[main][j.c.n.n.o.r.p.d.c.QuestionDialog]
        else if ((lineInfo.message contains "Dialog opened.") || (lineInfo.message contains "Opened.")) {
          getWindowName(lineInfo.message, lineInfo.clazz) match {
            case Some(windowName) =>
              windowBuffer += Window(windowName, LineTime(lineNo, lineInfo.datetime))
            case None => logger.warn(s"${fileInfo.fileName}:$lineNo Couldn't find windowName from message.")
          }
        }
        else if (lineInfo.message contains "Dialog closed."){
          getWindowName(lineInfo.message, lineInfo.clazz) match {
            case Some(windowName) =>
              windowBuffer.zipWithIndex.reverseIterator.find {
                case (w, _) => w.end.isEmpty && w.name.equals(windowName)
                } match {
                case Some((window, index)) =>
                  windowBuffer.update(index, window.copy(end = Some(LineTime(lineNo, lineInfo.datetime))))
                case None =>
                  logger.warn(s"${fileInfo.fileName}:$lineNo Couldn't find window from ListBuffer.")
              }
            case None => logger.warn(s"${fileInfo.fileName}:$lineNo Couldn't find windowName from message.")
          }

        }
        else if ((lineInfo.message contains "Button event ends") || (lineInfo.message contains "Button Pressed")) {
          getButtonAction(lineInfo.message) match {
            case Some(action) =>
              buttonEventBuffer += ButtonEvent(action, LineTime(lineNo, lineInfo.datetime))
            case None => logger.warn(s"${fileInfo.fileName}:$lineNo Couldn't find action from message.")
          }
        }
      }
      stream.close()

      for ( w <- windowBuffer ){

      }



      val outpathLog = outputdir.resolve(getObjFile(path.getFileName.toFile.toString, "Log"))
      val ostreamLog = new ObjectOutputStream(Files.newOutputStream(outpathLog))
      val log = Log(0L, fileInfo.appName, fileInfo.computer, fileInfo.userId, fileInfo.tradeDate, fileInfo.time)
      using(ostreamLog){ os =>
        os.writeObject(log)
      }

      //型を変換しないとIterableとなりSortができない
      val orgList = (for ((_, v) <- windowDetailMap) yield v.last).toList
      val convertedList = for {
        w <- orgList
        newHandler = w.handler.map(n => getLastAndDelNo(n))
        newWindowName = w.handler.map(n => getLastAndDelNo(n))
      } yield w.copy(handler = newHandler, windowName = newWindowName)
      val windowDetailList = convertedList.sortBy(_.lineNo)
      val outpath = outputdir.resolve(getObjFile(path.getFileName.toFile.toString, "Detail"))
      val ostream = new ObjectOutputStream(Files.newOutputStream(outpath))
      //    os.writeObject(windowDetailList)
      using(ostream){ os =>
        windowDetailList.foreach( w => {
          os.writeObject(w)
        })
      }
      val istream = new ObjectInputStream(Files.newInputStream(outpath))
      using(istream){is =>
        Iterator.continually(is.readObject()).takeWhile(_ != null).foreach(v => logger.info(v.toString))
      }
    }
  }

  private trait Finder[T] { def find: Option[T] }
  private abstract class WindowFinder[T](window: Window, val targetList: ListBuffer[T]) extends Finder[T]
  private trait IterateFinder[T] extends Finder[T] {
    def find: Option[T] = iterate.find(predicate)
    abstract def iterate: Iterator[T]
    abstract def predicate: T => Boolean
  }
  private trait UpwardIterator[T <: Naming with Starting] extends WindowFinder[T] with IterateFinder[T] {
    def iterate: Iterator[T] =  targetList.reverseIterator
    def predicate: T => Boolean = t => t.start.lineNo < window.start.lineNo
  }
  private class MaxLineNoFinder[T <: Starting](finders: Finder[T]*) extends Finder[T] {
    def find: Option[T] = {
      for {
        obj <- finders.map(_.find)
        out = obj.reduceLeft((a, b) => if (a.start.lineNo > b.start.lineNo) a else b
      }
        yield )
    }
  }


  /*private trait UpwardFinder[T <: Naming with Starting] extends Finder[T]  {
    def find: T = {
      targetList.reverseIterator.find(t => t.start.lineNo < window.start.lineNo)
    }
  }*/

  // WindowがNewSplitの場合、SmartSplitWindowがActivator
  // windowListも検索。SmartSplitが先に見つかったらそっち
  private class FinderFactory {
    def create[T](window: Window, targetList: ListBuffer[T])(implicit m: ClassTag[T]): Finder[T] = {
      val clazz = m.runtimeClass
      if (clazz.equals(classOf[Handler])) {
        new Finder[Handler](window, targetList) with UpwardIterator[Handler]
      }
    }
  }
}

object Keywords {
  val OBJ_SUFFIX = ".obj"
  val LOG_SUFFIX = "_Log"
  val WINDOW_DETAIL_SUFFIX = "_WindowDetail"
}

object Utils {
  import Keywords._
  def getObjFile(name: String, suffix: String): String = {
    getBase(name) + suffix + OBJ_SUFFIX
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
    val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).(log|zip)$""".r
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
    import Keywords.OBJ_SUFFIX
    val objs = f(sheet)
    val outpath = outputdir.resolve(sheet.getSheetName + OBJ_SUFFIX)
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