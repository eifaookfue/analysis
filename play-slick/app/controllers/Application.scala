package controllers

import javax.inject.Inject

import dao.WindowDetailDAO
import models.WindowDetail
import play.api.data.Form
import play.api.data.Forms.{ date, longNumber, mapping, nonEmptyText, optional }
import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, ControllerComponents, Flash, RequestHeader }
import views.html

import scala.concurrent.ExecutionContext

/** Manage a database of computers. */
class Application @Inject() (
    windowDetailDao: WindowDetailDAO,
    controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) with I18nSupport {

  /** This result directly redirect to the application home.*/
  val Home = Redirect(routes.Application.list(0, 2, ""))

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
  def list(page: Int, orderBy: Int, filterHandler: String, filterWindowName: String) = Action.async { implicit request =>
    val windowDetails = windowDetailDao.list(page = page, orderBy = orderBy, filterHandler = ("%" + filterHandler + "%"), filterWindowName = ("%" + filterWindowName + "%"))
    windowDetails.map(cs => Ok(html.list(cs, orderBy, filterHandler, filterWindowName)))
  }

}
