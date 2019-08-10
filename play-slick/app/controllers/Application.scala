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
  val Home = Redirect(routes.Application.list(0, 2, None, None))

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
  def list(page: Int, orderBy: Int, filterHandler: Option[String], filterWindowName: Option[String]) = Action.async { implicit request =>
    //画面のサーチボックスに何も入力しなかった場合、NoneではなくSome()が送られてきてしまうため、Option型に変更
    val windowDetails = windowDetailDao.list(page = page, orderBy = orderBy, filterHandler = filterHandler.filter(_.trim.nonEmpty).map("%" + _ + "%"), filterWindowName = filterWindowName.filter(_.trim.nonEmpty).map("%" + _ + "%"))
    windowDetails.map(cs => Ok(html.list(cs, orderBy, filterHandler, filterWindowName)))
  }

  def analyze(pathname: String) = Action.async { implicit request =>
    val page = 0
    val orderBy = 0
    val filterHandler = None
    val filterWindowName = None

    for {
      _ <- windowDetailDao.analyze(pathname)
    } yield Home.flashing("success" -> "successa")

    //windowDetails.map(cs => Ok(html.list(cs, orderBy, filterHandler, filterWindowName)))

  }

}
