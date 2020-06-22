package controllers

import java.nio.file.{Files, Paths}
import java.sql.Timestamp

import dao.{E9nDAO, WindowDetailDAO, WindowSliceDAO, WindowUserDAO}
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.client.E9n
import models._
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import views.html

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Properties, Random}

class Application @Inject() (
    windowDetailDao: WindowDetailDAO,
    windowSliceDao: WindowSliceDAO,
    windowUserDao: WindowUserDAO,
    e9nDao: E9nDAO,
    controllerComponents: ControllerComponents,
    config: Configuration
)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with I18nSupport {

  val windowDetailTblRequestForm = Form(
    mapping(
      "draw" -> number,
      "columns[0][search][value]" -> text,
      "columns[1][search][value]" -> text,
      "columns[2][search][value]" -> text,
      "columns[3][search][value]" -> text,
      "columns[4][search][value]" -> text,
      "columns[5][search][value]" -> text,
      "columns[6][search][value]" -> text,
      "order[0][column]" -> number,
      "order[0][dir]" -> text,
      "start" -> number,
      "length" -> number,
      "search[value]" -> text,
      "search[regex]" -> boolean)(WindowDetailTblRequestParams.apply)(WindowDetailTblRequestParams.unapply)
  )

  val windowSliceTblRequestForm = Form(
    mapping(
      "draw" -> number,
      "columns[0][search][value]" -> text,
      "columns[1][search][value]" -> text,
      "columns[2][search][value]" -> text,
      "order[0][column]" -> number,
      "order[0][dir]" -> text,
      "start" -> number,
      "length" -> number,
      "search[value]" -> text,
      "search[regex]" -> boolean)(WindowSliceTblRequestParams.apply)(WindowSliceTblRequestParams.unapply)
  )

  val e9nTblRequestForm = Form(
    mapping(
      "draw" -> number,
      "columns[0][search][value]" -> text,
      "columns[1][search][value]" -> text,
      "columns[2][search][value]" -> text,
      "order[0][column]" -> number,
      "order[0][dir]" -> text,
      "start" -> number,
      "length" -> number,
      "search[value]" -> text,
      "search[regex]" -> boolean)(E9nTblRequestParams.apply)(E9nTblRequestParams.unapply)
  )

  val e9nDetailTblRequestForm = Form(
    mapping(
      "draw" -> number,
      "columns[0][search][value]" -> text,
      "columns[1][search][value]" -> text,
      "columns[2][search][value]" -> text,
      "columns[3][search][value]" -> text,
      "columns[4][search][value]" -> text,
      "columns[5][search][value]" -> text,
      "order[0][column]" -> number,
      "order[0][dir]" -> text,
      "start" -> number,
      "length" -> number,
      "search[value]" -> text,
      "search[regex]" -> boolean)(E9nDetailTblRequestParams.apply)(E9nDetailTblRequestParams.unapply)
  )

  /** This result directly redirect to the application home.*/
  val Home: Result = Redirect(routes.Application.dashboard_client())

  /** Describe the computer form (used in both edit and create screens).*/

  // -- Actions

  /** Handle default path requests, redirect to computers list */
  def index: Action[AnyContent] = Action { Home }

  val r: Random = Random
  val userNames: Seq[String] = Seq("nakamura-s", "miyazaki-m", "saiki-c", "hori-n", "shimizu-r")
  val windowNames: Seq[String] = Seq("NewOrderSingle", "NewSplit", "NewExecution", "OrderDetail")
  val e9ns: Seq[String] = Seq("IllegalArgumentException", "RuntimeException", "TimeoutException")

  val windowCountByUsers: Seq[WindowCountByUser] = for {
    _ <- 1 to 100
    userName = userNames(r.nextInt(userNames.length))
    windowName = windowNames(r.nextInt(windowNames.length))
    count = r.nextInt(1000)
    windowCount = WindowCountByUser(userName, windowName, count)
  } yield windowCount

  val e9nList: Seq[E9n] = for {
    e9nId <- 1 to 100
    message = e9ns(r.nextInt(e9ns.length))
    count = r.nextInt(1000)
    e9n = E9n(e9nId, message, 0, count, new Timestamp(new java.util.Date().getTime))
  } yield e9n

  def dashboard_client: Action[AnyContent] = Action.async { implicit request =>
    val windowCountBySlice = windowSliceDao.list
    val windowCountByDate = windowDetailDao.windowCountByDate

    for {
      slice <- windowCountBySlice
      sliceJson = Json.toJson(slice)
      _ = println(sliceJson)
      date <- windowCountByDate
      dateJson = Json.toJson(date)
    } yield Ok(html.dashboard_client(sliceJson, dateJson))
  }

  def dashboard_server: Action[AnyContent] = Action {
    NotFound
  }

  def windowDetail: Action[AnyContent] = Action { implicit request =>
    Ok(html.window_detail("")(request))
  }

  def windowDetailTable(): Action[AnyContent] = Action.async { implicit request =>
    println(s"request=${request.body}")
    windowDetailTblRequestForm.bindFromRequest.fold(
      _ =>
        Future.successful(InternalServerError("Oops")),
      params =>
        for {
          recordsTotal <- windowDetailDao.count
          recordsFiltered <- windowDetailDao.count(params)
          seq <- windowDetailDao.list(params)
          w = WindowDetailTblResponse(params.draw, recordsTotal, recordsFiltered, seq)
          json = Json.toJson(w)
        } yield Ok(json)
    )

  }

  def fileDownload(logId: Int): Action[AnyContent] = Action {
    val (tradeDate, fileName) = Await.result(windowDetailDao.fileName(logId), 10.seconds)
    val fileDir = config.underlying.getString("logDir")
    val parent = Paths.get(fileDir).resolve(tradeDate)
    val file1 = parent.resolve(fileName)
    val file2 = parent.resolve(fileName.replace("log", "zip"))
    val target = if (Files.exists(file1)) file1 else file2
    Ok.sendFile(
      content = target.toFile,
      inline = false
    )
  }

  def windowCountTable(): Action[AnyContent] = Action.async { implicit request =>
    println(s"request=${request.body}")
    windowSliceTblRequestForm.bindFromRequest.fold(
      _ =>
        Future.successful(InternalServerError("Oops")),
      params =>
        for {
          recordsTotal <- windowUserDao.count
          recordsFiltered <- windowUserDao.count(params)
          seq <- windowUserDao.list(params)
          w = WindowCountByUserData(params.draw, recordsTotal, recordsFiltered, seq)
          json = Json.toJson(w)
        } yield Ok(json)
    )
  }

  def e9nDetail(e9nId: Option[Int]): Action[AnyContent] = Action { implicit request =>
    Ok(html.e9n_detail(e9nId)(request))
  }

  def e9nDetailTable(): Action[AnyContent] = Action.async { implicit request =>
    println(s"request=${request.body}")
    e9nDetailTblRequestForm.bindFromRequest.fold(
      _ =>
        Future.successful(InternalServerError("Oops")),
      params =>
        for {
          recordsTotal <- e9nDao.count
          recordsFiltered <- e9nDao.count(params)
          seq <- e9nDao.e9nDetailList(params)
          response = E9nDetailTblResponse(params.draw, recordsTotal, recordsFiltered, seq)
          json = Json.toJson(response)
        } yield Ok(json)
    )
  }

  def e9nListTable(): Action[AnyContent] = Action.async { implicit request =>
    e9nTblRequestForm.bindFromRequest.fold(
      _ =>
        Future.successful(InternalServerError("Oops")),
      params =>
        for {
          recordsTotal <- e9nDao.count
          recordsFiltered <- e9nDao.count(params)
          seq <- e9nDao.e9nList(params)
          response = E9nTblResponse(params.draw, recordsTotal, recordsFiltered, seq)
          json = Json.toJson(response)
        } yield Ok(json)
    )
  }

  def e9nStackTrace(e9nId: Int): Action[AnyContent] = Action.async {
    println(s"$e9nId request accepted.")
    for {
      seq <- e9nDao.e9nStackTraceList(e9nId)
      traces = seq.map(_.message).mkString("<br>" + Properties.lineSeparator)
      _ = println(traces)
    } yield Ok(traces)
  }

}