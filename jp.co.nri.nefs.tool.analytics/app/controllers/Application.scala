package controllers

import java.nio.file.Paths

import dao.{WindowDetailDAO, WindowSliceDAO}
import javax.inject.Inject
import models.{Params, WindowCountBySlice}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import views.html

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
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

  def dashboard_client = Action {
    windowSliceDao.list.foreach(println)

    val r = Random
    val windowCount = for {
      hour <- 6 to 17
      minute <- 0 to 5
      slice =  f"$hour%02d" + ":" + f"${minute*10}%02d"
      w = WindowCountBySlice(slice, r.nextInt(100), r.nextInt(100), r.nextInt(100))
    } yield w

    Ok(html.dashboard_client(Json.toJson(windowCount)))
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


}
