package controllers

import java.sql.Timestamp
import javax.inject.Inject
import dao.WindowDetailDAO
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
  val Home: Result = Redirect(routes.Application.list(0, 2, None, None))

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
  def list(page: Int, orderBy: Int,
           filterAppName: Option[String],
           filterComputerName: Option[String],
           filterUserId: Option[String],
           filterTradeDate: Option[String],
           filterLineNo: Option[Long],
           filterHandler: Option[String],
           filterWindowName: Option[String],
           filterDestinationType: Option[String],
           filterAction: Option[String],
           filterMethod: Option[String],
           filterTime: Option[Long],
           filterStartupTime: Option[Long]
          ) = Action.async { implicit request =>
    //画面のサーチボックスに何も入力しなかった場合、NoneではなくSome()が送られてきてしまうため、Option型に変更
    val windowDetails = windowDetailDao.list(page = page, orderBy = orderBy,
      filterAppName = filterAppName.filter(_.trim.nonEmpty).map("%" + _ + "%"),
      filterComputerName = filterComputerName.filter(_.trim.nonEmpty).map("%" + _ + "%"),
      filterUserId = filterUserId.filter(_.trim.nonEmpty).map("%" + _ + "%"),
      filterTradeDate = filterTradeDate.filter(_.trim.nonEmpty).map("%" + _ + "%"),
      filterLineNo = filterLineNo,
      filterHandler = filterHandler.filter(_.trim.nonEmpty).map("%" + _ + "%"),
      filterWindowName = filterWindowName.filter(_.trim.nonEmpty).map("%" + _ + "%"),
      filterDestinationType = filterDestinationType.filter(_.trim.nonEmpty).map("%" + _ + "%"),
      filterAction = filterAction.filter(_.trim.nonEmpty).map("%" + _ + "%"),
      filterMethod = filterMethod.filter(_.trim.nonEmpty).map("%" + _ + "%"),
      filterTime = None: Option[Timestamp],
      filterStartupTime = filterStartupTime)
    windowDetails.map(cs => Ok(html.list(cs, orderBy, filterAppName,
        filterComputerName, filterUserId, filterTradeDate, filterLineNo,
        filterHandler, filterWindowName, filterDestinationType, filterAction, filterMethod,
      None:Option[Timestamp], filterStartupTime)))
  }

  def load(name: String, isRecreate: Boolean) = Action.async { implicit request =>
    println("called")
    for {
      _ <- windowDetailDao.load(name, isRecreate)
    } yield Home.flashing("success" -> "success")

  }

}
