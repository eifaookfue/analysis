package controllers

import java.nio.file.{Files, Paths}
import java.sql.Timestamp

import dao.{WindowDetailDAO, WindowSliceDAO, WindowUserDAO}
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
import scala.util.Random

class Application @Inject() (
    windowDetailDao: WindowDetailDAO,
    windowSliceDao: WindowSliceDAO,
    windowUserDao: WindowUserDAO,
    controllerComponents: ControllerComponents,
    config: Configuration
)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with I18nSupport {
/*
      val order0Column = intBinder.bind("order[0][column]", params)
      val order0Dir = strBinder.bind("order[0][dir]", params)
      val start = intBinder.bind("start", params)
      val length = intBinder.bind("length", params)
      val searchValue = strBinder.bind("search[value]", params)
      val searchRegex = boolBinder.bind("search[regex]", params)
      println(s"bind called. draw=$draw")
 */

  val windowDetailTableForm = Form(
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
    e9n = E9n(e9nId, message, 0, count)
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
    Ok(html.window_detail(request))
  }

  def windowDetailTable(): Action[AnyContent] = Action.async { implicit request =>
    println(s"request=${request.body}")
    windowDetailTableForm.bindFromRequest.fold(
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

  def windowCountTable(params: WindowCountTableParams): Action[AnyContent] = Action.async { implicit request =>
    println(request)
    println(params)
    //val filtered = windowCountByUsers.filter(uw => uw.windowName.contains(params.searchValue) || uw.userName.contains(params.searchValue))
    //val sorted  = WindowCountByUser.sort(filtered, params.order0Column, params.order0Dir)
    for {
      recordsTotal <- windowUserDao.count
      recordsFiltered <- windowUserDao.count(params)
      seq <- windowUserDao.list(params)
      w = WindowCountByUserData(params.draw, recordsTotal, recordsFiltered, seq)
      json = Json.toJson(w)
    } yield Ok(json)
  }

  def e9nListTable(params: E9nListTableParams): Action[AnyContent] = Action { implicit request =>
    println(request)
    println(params)
    val filtered = e9nList.filter(_.e9nHeadMessage.contains(params.searchValue))
    val sorted  = E9n.sort(filtered, params.order0Column, params.order0Dir)
    val data = E9nListData(params.draw, 100, sorted.length, sorted.slice(params.start, params.start + params.length))
    println(Json.toJson(data).toString())
    Ok(Json.toJson(data))
  }


}
