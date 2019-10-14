package controllers

import dao.WindowDetailDAO
import javax.inject.Inject
import models.Params
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import views.html

import scala.concurrent.ExecutionContext

/** Manage a database of computers. */
class Application @Inject() (
    windowDetailDao: WindowDetailDAO,
    controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with I18nSupport {

  /** This result directly redirect to the application home.*/
  val Home: Result = Redirect(routes.Application.list(Params()))

  /** Describe the computer form (used in both edit and create screens).*/

  // -- Actions

  /** Handle default path requests, redirect to computers list */
  def index = Action { Home }

  /**
   * Display the paginated list of computers.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on computer names
   */
  def list(params: Params) = Action.async { implicit request =>
    val windowDetails = windowDetailDao.list(params)
    windowDetails.map(wd => Ok(html.list(wd, params)))
  }

  def load(name: String, isRecreate: Boolean) = Action.async { implicit request =>
    println("called")
    for {
      _ <- windowDetailDao.load(name, isRecreate)
    } yield Home.flashing("success" -> "success")

  }

}
