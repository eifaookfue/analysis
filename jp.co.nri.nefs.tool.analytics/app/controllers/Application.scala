package controllers

import java.nio.file.{Files, Paths}
import java.sql.Timestamp
import java.time.LocalDateTime

import dao._
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.client.STATUS
import models._
import play.api.{Configuration, Logging}
import play.api.data.{Form, FormError}
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
    preCheckSummaryDao: PreCheckSummaryDAO,
    controllerComponents: ControllerComponents,
    config: Configuration
)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents)
  with I18nSupport with Logging {

  import play.api.data.format.Formatter
  import play.api.data.format.Formats._
  implicit object OptionIntFormatter extends Formatter[Option[Int]] {
    override val format: Option[(String, Seq[Any])] = Some("format.optionInt", Nil)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[Int]] =
      parsing(Option(_).filter(_.trim.nonEmpty).map(_.toInt), "error.optionInt", Nil)(key, data)

    override def unbind(key: String, value: Option[Int]): Map[String, String] =
      Map(key -> value.map(_.toString).getOrElse(""))
  }

  implicit object statusFormatter extends Formatter[Option[STATUS]] {
    override val format: Option[(String, Seq[Any])] = Some("format.status", Nil)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[STATUS]] =
      parsing(Option(_).filter(_.trim.nonEmpty).map(STATUS.valueOf), "error.status", Nil)(key, data)

    override def unbind(key: String, value: Option[STATUS]): Map[String, String] =
      Map(key -> value.map(_.toString).getOrElse(""))

  }

  implicit object OptionTimestampFormatter extends Formatter[Option[Timestamp]] {
    override val format: Option[(String, Seq[Any])] = Some("format.optionInt", Nil)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[Timestamp]] =
      parsing(Option(_).filter(_.trim.nonEmpty).map(s => Timestamp.valueOf(LocalDateTime.parse(s))),
        "error.optionTimestamp", Nil)(key, data)

    override def unbind(key: String, value: Option[Timestamp]): Map[String, String] =
      Map(key -> value.map(_.toString).getOrElse(""))
  }

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
      "columns[0][search][value]" -> of[Option[Int]],
      "columns[1][search][value]" -> text,
      "columns[3][search][value]" -> of[Option[Int]],
      "columns[4][search][value]" -> text.transform[STATUS](STATUS.valueOf, _.toString),
      "order[0][column]" -> number,
      "order[0][dir]" -> text,
      "start" -> number,
      "length" -> number,
      "search[value]" -> text,
      "search[regex]" -> boolean)(E9nTblRequestParams.apply)(E9nTblRequestParams.unapply)
  )

  val e9nAuditTblRequestForm = Form(
    mapping(
      "draw" -> number,
      "columns[0][search][value]" -> of[Option[Int]],
      "columns[1][search][value]" -> of[Option[STATUS]],
      "columns[2][search][value]" -> text,
      "columns[3][search][value]" -> text,
      "columns[4][search][value]" -> of[Option[Timestamp]],
      "order[0][column]" -> number,
      "order[0][dir]" -> text,
      "start" -> number,
      "length" -> number,
      "search[value]" -> text,
      "search[regex]" -> boolean
    )(E9nAuditTblRequestParams.apply)(E9nAuditTblRequestParams.unapply)
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

  val preCheckTblRequestForm = Form(
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
      "search[regex]" -> boolean)(PreCheckTblRequestParams.apply)(PreCheckTblRequestParams.unapply)
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
      formWithErrors => {
        println(formWithErrors)
        Future.successful(InternalServerError("Oops"))},
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

  def e9nAudit(e9nId: Int): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"$request called.")
    e9nAuditTblRequestForm.bindFromRequest.fold(
      error => {
        logger.warn(s"error=$error")
        Future.successful(InternalServerError("Oops"))
      },
      params => {
        logger.info(s"params = $params")
        for {
          recordsTotal <- Future(1)
          recordsFiltered <- Future(1)
          seq <- e9nDao.e9nAudit(e9nId)
          data = seq.map(s => E9nAuditTbl(s.audit.e9nId, s.audit.status, s.audit.comment, s.audit.updatedBy, s.updateTime))
          response = E9nAuditTblResponse(params.draw, recordsTotal, recordsFiltered, data)
          json = Json.toJson(response)
          _ = logger.info(s"json = $json")
        } yield Ok(json)
      }
    )
  }

  def preCheckSummaryTable(): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"request=$request")
    preCheckTblRequestForm.bindFromRequest.fold(
      error => {
        logger.warn(s"error=$error")
        Future.successful(InternalServerError("Oops"))
      },
      params => {
        logger.info(s"params=$params")
        for {
          recordsTotal <- preCheckSummaryDao.count
          recordsFiltered <- preCheckSummaryDao.count(params)
          seq <- preCheckSummaryDao.list(params)
          response = PreCheckTblResponse(params.draw, recordsTotal, recordsFiltered, seq)
          _ = println(response)
          json = Json.toJson(response)
        } yield Ok(json)
      }
    )
  }
}