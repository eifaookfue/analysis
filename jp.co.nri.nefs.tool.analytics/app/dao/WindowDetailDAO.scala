package dao

import java.io.{EOFException, ObjectInputStream}
import java.nio.file.{Files, Path, Paths}

import javax.inject.{Inject, Singleton}
import jp.co.nri.nefs.tool.analytics.store.client.model._
import jp.co.nri.nefs.tool.analytics.store.common.model.User
import models.{Page, Params}
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton()
class WindowDetailDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, config: Configuration)(implicit executionContext: ExecutionContext)
  extends WindowDetailComponent with LogComponent with UserComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val logs = TableQuery[Logs]
  val windowDetails = TableQuery[WindowDetails]
  val users = TableQuery[Users]

  /** Count all computers. */
  def count(): Future[Int] = {
    // this should be changed to
    // db.run(computers.length.result)
    // when https://github.com/slick/slick/issues/1237 is fixed
    db.run(windowDetails.map(_.windowName).length.result)
  }
  /** Count computers with a filter. */
  def count(filterHandler: Option[String], filterWindowName: Option[String]): Future[Int] = {
    db.run(windowDetails.filter { windowDetail => windowDetail.windowName.toLowerCase like filterHandler.map(_.toLowerCase).getOrElse("") }.length.result)
  }


  /** Return a page of WindowDetail */
  def list(params: Params, pageSize: Int = 10): Future[Page[(Log, WindowDetail, Option[User])]] = {
    val page = params.page
    val orderBy = params.orderBy.getOrElse(1)
    val offset = page * pageSize
    //windowNameはOptional。getOrElseを使わないとvalue || is not a member of slick.lifted.Rep[_1]がでてしまう。
    //getOrElse("")とすることで、ifnull(`WINDOW_NAME`,'') like **となる
    // Log,WindowDetail両方にレコードがあった場合のみ出力するためInnerJoinを用いる
    val query1 = for{ ((l, w), u) <- logs join windowDetails on (_.logId === _.logId) joinLeft users on (_._1.userId === _.userId)
      if List(
        params.appName.filter(_.trim.nonEmpty).map(l.appName like "%" + _ + "%"),
        params.computerName.filter(_.trim.nonEmpty).map(l.computerName like "%" + _ +  "%"),
        params.userName.filter(_.trim.nonEmpty).map(u.map(_.userName).getOrElse("") like "%" + _ +  "%"),
        params.tradeDate.filter(_.trim.nonEmpty).map(l.tradeDate like "%" + _ +  "%"),
        params.lineNo.map(w.lineNo === _),
        params.activator.filter(_.trim.nonEmpty).map(w.activator.getOrElse("") like "%" + _ +  "%"),
        params.windowName.filter(_.trim.nonEmpty).map(w.windowName.getOrElse("") like "%" + _ +  "%"),
        params.destinationType.filter(_.trim.nonEmpty).map(w.destinationType.getOrElse("") like "%" + _ +  "%"),
        params.action.filter(_.trim.nonEmpty).map(w.action.getOrElse("") like "%" + _ +  "%"),
        params.method.filter(_.trim.nonEmpty).map(w.method.getOrElse("") like "%" + _ +  "%"),
        params.time.map(w.time === _),
        params.startupTime.map(w.startupTime.getOrElse(0L) === _)
      ).collect({ case Some(criteria) => criteria }).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
    } yield (l, w, u)

    // https://stackoverflow.com/questions/24190955/slick-dynamic-sortby-in-a-query-with-left-join
    // なぜかsortByの引数の中でtupleが利用できない
    val query2 = (orderBy match {
      case 1 => query1.sortBy(_._1.logId.asc)
      case -1 => query1.sortBy(_._1.logId.desc)
      case 2 => query1.sortBy(_._1.appName.asc)
      case -2 => query1.sortBy(_._1.appName.desc)
      case 3 => query1.sortBy(_._1.computerName.asc)
      case -3 => query1.sortBy(_._1.computerName.desc)
      case 4 => query1.sortBy(_._3.map(_.userName).asc.nullsFirst)
      case -4 => query1.sortBy(_._3.map(_.userName).desc.nullsLast)
      case 5 => query1.sortBy(_._1.tradeDate.asc)
      case -5 => query1.sortBy(_._1.tradeDate.desc)
      case 6 => query1.sortBy(_._2.lineNo.asc)
      case -6 => query1.sortBy(_._2.lineNo.desc)
      case 7 => query1.sortBy(_._2.activator.asc)
      case -7 => query1.sortBy(_._2.activator.desc)
      case 8 => query1.sortBy(_._2.windowName.asc.nullsFirst)
      case -8 => query1.sortBy(_._2.windowName.desc.nullsLast)
      case 9 => query1.sortBy(_._2.destinationType.asc.nullsFirst)
      case -9 => query1.sortBy(_._2.destinationType.desc.nullsLast)
      case 10 => query1.sortBy(_._2.action.asc.nullsFirst)
      case -10 => query1.sortBy(_._2.action.desc.nullsLast)
      case 11 => query1.sortBy(_._2.method.asc.nullsFirst)
      case -11 => query1.sortBy(_._2.method.desc.nullsLast)
      case 12 => query1.sortBy(_._2.time.asc.nullsFirst)
      case -12 => query1.sortBy(_._2.time.desc.nullsLast)
      case 13 => query1.sortBy(_._2.startupTime.asc.nullsFirst)
      case -13 => query1.sortBy(_._2.startupTime.desc.nullsLast)
      case _ => query1
    }).drop(offset)
      .take(pageSize)

    for {
      totalRows <- db.run(query1.length.result)
      //list = query.result.map { rows => rows.collect { case (computer, id, Some(name)) => (computer, Company(id, name)) } }
      list = query2.result
      result <- db.run(list)
    } yield Page(result, page, offset, totalRows)
  }

  private def using[A <: java.io.Closeable, B](s: A)(f: A => B): B = {
    try { f(s) } finally { s.close() }
  }

  private def deserializeObject[T >: Null](inputstream: ObjectInputStream): T = {
    try {
      inputstream.readObject().asInstanceOf[T]
    } catch { case _ : EOFException => null }
  }

  private def deserializeObjects[T >: Null](path: Path): List[T] = {
    val istream = new ObjectInputStream(Files.newInputStream(path))
    using(istream) { is =>
      Iterator.continually(deserializeObject[T](is)).takeWhile(_ != null).toList
    }
  }

  private def recreate(): Unit = {
    for {tableQuery <- Seq(logs, windowDetails, users)}{
      val schema = tableQuery.schema
      println("create statements")
      schema.create.statements.foreach(println)
      val setup = DBIO.seq(
        schema.dropIfExists,
        schema.createIfNotExists
      )
      val setupFuture = db.run(setup)
      Await.result(setupFuture, Duration.Inf)
    }
  }

  def load(isRecreate: Boolean): Unit = {
    import jp.co.nri.nefs.tool.analytics.store.client.Keywords._
    if (isRecreate) {
      recreate()
    }

    val path = Paths.get(config.underlying.getString("caseDir"))

    val bases = for {
      file <- Files.list(path).iterator().asScala.toList
      name = file.getFileName.toFile.toString
      if name.contains(LOG_SUFFIX)
      base = name.replace(LOG_SUFFIX, "").replace(OBJ_EXTENSION, "")
    } yield base

    for {
      base <- bases
      logPath = path.resolve(base + LOG_SUFFIX + OBJ_EXTENSION)
      _ = println(logPath)
      // Logは1レコードしか存在しない
      logObj = deserializeObjects[Log](logPath).head
      detailPath = path.resolve(base + WINDOW_DETAIL_SUFFIX + OBJ_EXTENSION)
      windowDetailObjs = deserializeObjects[WindowDetail](detailPath)
      action = (
        for {
          logId <- (logs returning logs.map(_.logId)) += logObj
          _ <- windowDetails ++= windowDetailObjs.map(w => w.copy(logId = logId))
        } yield ()).transactionally
    } {
      try {
        val f = db.run(action)
        Await.result(f, Duration.Inf)
      } catch {
        case e: Exception => println(e)
      }
    }
  }

}