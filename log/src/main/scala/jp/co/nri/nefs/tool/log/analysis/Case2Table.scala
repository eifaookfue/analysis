package jp.co.nri.nefs.tool.log.analysis

import java.io.{EOFException, ObjectInputStream}
import java.nio.file.{Files, NoSuchFileException, Path, Paths}
import java.sql.Timestamp
import java.util.stream.Collectors

import jp.co.nri.nefs.tool.log.common.model.WindowDetail
//import slick.driver.MySQLProfile.api._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.collection.Map
import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait WindowDetailComponent {
  val dc = DatabaseConfig.forConfig[JdbcProfile]("mydb")
  import dc.profile.api._
  class WindowDetails(tag: Tag) extends Table[WindowDetail](tag, "WINDOW_DETAIL") {
    def appName = column[String]("APPLICATION_NAME", O.Length(20))
    def computerName = column[String]("COMPUTER_NAME", O.Length(20))
    def userId = column[String]("USERID", O.Length(6))
    def tradeDate = column[String]("TRADE_DATE", O.Length(8))
    def lineNo = column[Long]("LINE_NO", O.Length(20))
    def handler = column[String]("HANDLER")
    def windowName = column[Option[String]]("WINDOW_NAME")
    def destinationType = column[Option[String]]("DESTINATION_TYPE")
    def action = column[Option[String]]("ACTION")
    def method = column[Option[String]]("METHOD")
    def time = column[Timestamp]("TIME")
    def startupTime = column[Long]("STARTUP_TIME")
    def * = (appName, computerName, userId, tradeDate, lineNo, handler, windowName, destinationType, action, method, time, startupTime) <> (WindowDetail.tupled, WindowDetail.unapply)
    def idx_1 = index("idx_1", (appName, computerName, userId, tradeDate, lineNo), unique = true)
    //create unique index `idx_1` on `WINDOW_DETAIL` (`APPLICATION_NAME`(20),`COMPUTER_NAME`(20),`USERID`(6),`TRADE_DATE`(8),`LINE_NO`)
  }
}

class Case2Table() extends WindowDetailComponent {

  import dc.profile.api._
  val db = dc.db
  val windowDetails = TableQuery[WindowDetails]

  private def using[A <: java.io.Closeable, B](s: A)(f: A => B): B = {
    try { f(s) } finally { s.close() }
  }

  private def readWindowDetail(inputstream: ObjectInputStream): WindowDetail = {
    try {
      inputstream.readObject().asInstanceOf[WindowDetail]
    } catch { case _ : EOFException => null }
  }

  private def createWindowDetailListByFile(path: Path): List[WindowDetail] = {
    val istream = new ObjectInputStream(Files.newInputStream(path))
    using(istream) { is =>
      Iterator.continually(readWindowDetail(is)).takeWhile(_ != null).toList
    }
  }

  private def createWindowDetailListByDir(path: Path): List[WindowDetail] = {
    val files = Files.list(path).collect(Collectors.toList()).asScala.toList
    for ( f <- files; w <- createWindowDetailListByFile(f) ) yield w
  }

  def execute(windowDetailList: List[WindowDetail]){

    try {
      windowDetailList.foreach(windowDetail => {
        val setup = DBIO.seq(windowDetails += windowDetail)
        try {
          val setupFuture = db.run(setup)
          Await.result(setupFuture, Duration.Inf)
        } catch {
          case e: Exception => println(e)
        }
      })
    } finally db.close

  }

  def recreate(): Unit = {
    val schema = windowDetails.schema
    schema.create.statements.foreach(println)
    try {
      val setup = DBIO.seq(
        schema.dropIfExists,
        schema.createIfNotExists
      )
      val setupFuture = db.run(setup)
      Await.result(setupFuture, Duration.Inf)
    }finally db.close

  }

}

object Case2Table {
  type OptionMap = Map[Symbol, String]
  val usage = """
        Usage: jp.co.nri.nefs.tool.log.analysis.Case2Table [--recreate] [--inputdir dir | --file file]
        """

  def main(args: Array[String]): Unit = {
    val options = nextOption(Map(), args.toList)
    val (isRecreate, path) = getOption(options)
    /*val case2Table = new Case2Table()
    case2Table.execute(path)*/
    val case2Table = new Case2Table()
    if (isRecreate)
      case2Table.recreate()
    path match {
      //case Some(p) => case2Table.execute(p)
      case Some(p) =>
        val windowDetailList = if (p.toFile.isFile)
          case2Table.createWindowDetailListByFile(p)
        else
          case2Table.createWindowDetailListByDir(p)
        windowDetailList.foreach(println _)
        case2Table.execute(windowDetailList)
      case None =>
    }
  }

  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    list match {
      case Nil => map
      case "--recreate" :: tail =>
        nextOption(map ++ Map(Symbol("isRecreate") -> "TRUE"), tail)
      case "--inputdir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("inputdir") -> value), tail)
      case "--file" :: value :: tail =>
        nextOption(map ++ Map(Symbol("file") -> value), tail)
      case _ => println("Unknown option")
        println(usage)
        throw new java.lang.IllegalArgumentException
    }
  }

  def getOption(options: OptionMap): (Boolean, Option[Path]) = {
    val isRecreate = options.get(Symbol("isRecreate"))
    val inputdir = options.get(Symbol("inputdir"))
    val file = options.get(Symbol("file"))
    if ((isRecreate.isEmpty && inputdir.isEmpty && file.isEmpty) ||
      (inputdir.nonEmpty && file.nonEmpty)) {
      println(usage)
      throw new java.lang.IllegalArgumentException
    }

    val path = inputdir match {
      case Some(dir) =>
        val path = inputdir.map(Paths.get(_))
        if (!path.get.toFile.exists()){
          throw new NoSuchFileException(path.toString)
        }
        if (path.get.toFile.isFile){
          throw new java.lang.IllegalArgumentException("Not a directory")
        }
        path
      case None =>
        val path = file.map(Paths.get(_))
        if (path.nonEmpty) {
          if (!path.get.toFile.exists()){
            throw new NoSuchFileException(path.toString)
          }
          if (path.get.toFile.isDirectory){
            throw new java.lang.IllegalArgumentException("Not a file")
          }
        }
        path
    }
    (isRecreate.map(_ == "TRUE").getOrElse(false), path)
  }

}