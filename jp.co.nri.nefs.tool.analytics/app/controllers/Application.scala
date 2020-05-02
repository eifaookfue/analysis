package controllers

import java.nio.file.Paths

import dao.{WindowDetailDAO, WindowSliceDAO}
import javax.inject.Inject
import models._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import views.html

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

class Application @Inject() (
    windowDetailDao: WindowDetailDAO,
    windowSliceDao: WindowSliceDAO,
    controllerComponents: ControllerComponents,
    config: Configuration
)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with I18nSupport {

  /** This result directly redirect to the application home.*/
  val Home: Result = Redirect(routes.Application.dashboard_client())

  /** Describe the computer form (used in both edit and create screens).*/

  // -- Actions

  /** Handle default path requests, redirect to computers list */
  def index = Action { Home }

  val r: Random = Random
  val userNames: Seq[String] = Seq("nakamura-s", "miyazaki-m", "saiki-c", "hori-n", "shimizu-r")
  val windowNames: Seq[String] = Seq("NewOrderSingle", "NewSplit", "NewExecution", "OrderDetail")

  val windowCountByUsers: Seq[WindowCountByUser] = for {
    _ <- 1 to 100
    userName = userNames(r.nextInt(userNames.length))
    windowName = windowNames(r.nextInt(windowNames.length))
    count = r.nextInt(1000)
    windowCount = WindowCountByUser(userName, windowName, count)
  } yield windowCount

  def dashboard_client = Action.async { implicit request =>
    /*val windowCountBySlice = windowSliceDao.list
    val windowCountByDate = windowDetailDao.windowCountByDate*/

    val windowCountBySlice = Future {
      for {
        hour <- 6 to 17
        minute <- 0 to 5
        slice =  f"$hour%02d" + ":" + f"${minute*10}%02d"
        w = WindowCountBySlice(slice, r.nextInt(100), r.nextInt(100), r.nextInt(100))
      } yield w
    }

    val windowCountByDate = Future {
      for {
        month <- 1 to 12
        day <- 1 to 30
        tradeDate = "2020" + f"$month%02d" + f"$day%02d"
        w = WindowCountByDate(tradeDate, r.nextInt(100), r.nextInt(100), r.nextInt(100))
      } yield w
    }

    for {
      slice <- windowCountBySlice
      sliceJson = Json.toJson(slice)
      _ = println(sliceJson)
      date <- windowCountByDate
      dateJson = Json.toJson(date)
      _ = println(dateJson)
    } yield Ok(html.dashboard_client(sliceJson, dateJson))
  }

  def dashboard_server = Action {
    NotFound
  }

  def list(params: Params) = Action.async { implicit request =>
    val windowDetails = windowDetailDao.list(params)
    windowDetails.map(wd => Ok(html.list(wd, params)))
  }

  def fileDownload(logId: Int) = Action {
    val fileName = Await.result(windowDetailDao.fileName(logId), 10.seconds)
    val fileDir = config.underlying.getString("logDir")
    Ok.sendFile(
      content = Paths.get(fileDir).resolve(fileName).toFile,
      inline = false
    )
  }

  def ajaxCall(params: DataTableParams) = Action { implicit request =>
    println(request)
    println(params)
    val filtered = windowCountByUsers.filter(uw => uw.windowName.contains(params.searchValue) || uw.userName.contains(params.searchValue))
    val sorted  = WindowCountByUser.sort(filtered, params.order0Column, params.order0Dir)
    val data = WindowCountByUserData(params.draw, 100, sorted.length, sorted.slice(params.start, params.start + params.length))
    println(Json.toJson(data).toString())
    Ok(Json.toJson(data))
  }

}
