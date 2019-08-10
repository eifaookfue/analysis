package jp.co.nri.nefs.tool.log.analysis

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.nio.file.{Files, Path, Paths}
import java.sql.Timestamp
import java.util.Date
import java.util.stream.Collectors

import jp.co.nri.nefs.tool.log.common.utils.FileUtils
import jp.co.nri.nefs.tool.log.common.utils.ZipUtils
import jp.co.nri.nefs.tool.log.common.model.WindowDetail

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.collection._
import scala.collection.JavaConverters._


class Log2Case(outputdir: Path) {

  /**
    * キー：windowName
    * バリュー：Windowクラスのリスト。追加する必要があるためmutableなListBufferを用いる
    */
  private var windowDetailMap = Map[Option[String], ListBuffer[WindowDetail]]()

  private case class FileInfo(appName: String, env: String, computer: String, userId: String, startTime: String){
    val tradeDate: String = startTime.take(8)
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

    if (isZipFile(path.getFileName.toString)){
      val tmpdir = Files.createTempDirectory(path.getParent,"tmp")
      val target = tmpdir.resolve(path.getFileName.toString)
      Files.copy(path, target)
      ZipUtils.unzip(target)
      val paths = Files.list(tmpdir).filter(p => !p.getFileName.toString.endsWith(".zip")).collect(Collectors.toList())
      execute(paths.asScala.toList)
      FileUtils.delete(tmpdir)
    } else {
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
        Iterator.continually(is.readObject()).takeWhile(_ != null).foreach(println _)
      }
    }

  }

  def isZipFile(name : String): Boolean = {
    if (name.replace(getBase(name),"") == ".zip")
      true
    else
      false
  }
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
        Usage: jp.co.nri.nefs.tool.log.analysis.Log2Case [--searchdir dir | --file file] --outputdir dir
        """

  def main(args: Array[String]): Unit = {

    //lazy val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).log$""".r
    lazy val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).(log|zip)$""".r
    val options = nextOption(Map(), args.toList)
    val (isSearchDir, dirOrFile, outputdir) = getOption(options)
    val log2case = new Log2Case(Paths.get(outputdir))
    if (isSearchDir) {
      val paths = Files.walk(Paths.get(dirOrFile)).filter(_.toFile.isFile).filter(p => {
        p.getFileName.toString match {
          case regex(_, _, _, _, _, _) => true
          case _ =>
            println(s"skipped $p because not a valid format.")
            false
        }
      }).collect(Collectors.toList())
      log2case.execute(paths.asScala.toList)
    } else {
      log2case.execute(Paths.get(dirOrFile))
    }
  }

  /**
    *
    * @param options
    * @return searchdirが指定されているか
    */
  def getOption(options: OptionMap): (Boolean, String, String) = {
    val searchdir = options.get(Symbol("searchdir"))
    val file = options.get(Symbol("file"))
    val outpudir = options.get(Symbol("outputdir"))
    if ((searchdir.isEmpty && file.isEmpty) || (searchdir.nonEmpty && file.nonEmpty) || outpudir.isEmpty) {
      println(usage)
      throw new java.lang.IllegalArgumentException
    }
    (searchdir.nonEmpty, searchdir.getOrElse(file.get), outpudir.get)
  }

  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    list match {
      case Nil => map
      case "--searchdir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("searchdir") -> value), tail)
      case "--file" :: value :: tail =>
        nextOption(map ++ Map(Symbol("file") -> value), tail)
      case "--outputdir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("outputdir") -> value), tail)
      case _ => println("Unknown option")
        println(usage)
        sys.exit(1)
    }
  }
}