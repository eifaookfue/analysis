package controllers

import java.nio.file.{Files, Path, Paths}

import dao.WindowDetailDAO
import javax.inject.Inject
import models.Params
import play.api.i18n.I18nSupport
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import views.html

import scala.collection.JavaConverters._
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

  def load(isRecreate: Boolean) = Action { implicit request =>
    println("called")
    windowDetailDao.load(isRecreate)
    Ok("success")

  }

  /*def listPlaces = Action {
    val json = Json.toJson(Place.list)
    Ok(json)
  }*/

  case class Location(lat: Double, long: Double)
  //case class Place(name: String, location: Location)
  //case class Resident(name: String, age: Int, role: Option[String])
  case class NEWIOI(symbol: String, ioiqty: Int, ioiqualifier: String)
  case class Place(name: String, location: Location, residents: Seq[IOI])
  //case class Place(name: String, location: Location, residents: Seq[IOI])
  case class IOI(symbol: String, ioiqty: Int, ioiqualifier: String)
  //case class IOIS(name: String, iois: Seq[IOI])
  case class IOIS(iois: Seq[IOI])

  object Place {

    var list: List[Place]  =
      List(

        Place(
          "Sandleford",
          Location(51.377797, -1.318965),
          //Seq(Resident("a", 10, Some("role1")), Resident("b", 20, Some("role2")))
          Seq(IOI("a", 10, "A"), IOI("b", 20, "B"))
          //Seq(IOI("6501", 1000, "A"), IOI("6502", 2000, "B"))
        ),
        Place(
          "Watership Down",
          Location(51.235685, -1.309197),
          //Seq(Resident("c", 30, Some("role3")), Resident("d", 40, Some("role4")))
          Seq(IOI("c", 30, "C"), IOI("d", 40, "D"))
          //Seq(IOI("6503", 1000, "A"), IOI("6504", 2000, "B"))
        )
      )


    def save(place: Place) = {
      list = list ::: List(place)
    }
  }

  /*implicit val locationWrites: Writes[Location] =
    (JsPath \ "lat").write[Double].and((JsPath \ "long").write[Double])(unlift(Location.unapply))

  implicit val placeWrites: Writes[Place] =
    (JsPath \ "name").write[String].and((JsPath \ "location").write[Location])(unlift(Place.unapply))*/

  /*implicit val locationReads: Reads[Location] =
    (JsPath \ "lat").read[Double].and((JsPath \ "long").read[Double])(Location.apply _)*/

  /*implicit val placeReads: Reads[Place] =
    (JsPath \ "name").read[String].and((JsPath \ "location").read[Location])(Place.apply _)*/


  implicit val locationReads: Reads[Location] = (
    (JsPath \ "lat").read[Double](min(-90.0) keepAnd max(90.0)) and
      (JsPath \ "long").read[Double](min(-180.0) keepAnd max(180.0))
    )(Location.apply _)

  /*implicit val residentReads: Reads[Resident] = (
    (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "age").read[Int](min(0) keepAnd max(150)) and
      (JsPath \ "role").readNullable[String]
    )(Resident.apply _)*/

  // 2019/11/16 22:26 いったん消してみる
  /*implicit val ioiReads: Reads[IOI] = (
  (JsPath \ "symbol").read[String] and
    (JsPath \ "ioiqty").read[Int] and
    (JsPath \ "ioiqualifier").read[String]
  )(IOI.apply _)*/



  // 2019/11/16 22:23 いったん消してみる→復活
  implicit val ioiReads: Reads[IOI] = (
    (JsPath \ "symbol").read[String] and
      (JsPath \ "ioiqty").read[Int] and
      (JsPath \ "ioiqualifier").read[String]
    )(IOI.apply _)

  implicit val ioisReads: Reads[Seq[IOI]] = Reads.seq(ioiReads)

  // 2019/11/17 08:00 これはOK
/*  implicit val ioisReads: Reads[IOIS] =(
    (JsPath \ "name").read[String] and
    (JsPath \ "iois").read[Seq[IOI]]
  )(IOIS.apply _)*/

  // readが赤くなる
    //implicit val ioisReads: Reads[IOIS] = (JsPath \ "iois").read[Seq[IOI]](IOIS.apply _)

  //Expression of type Reads[Application.this.IOI] dosen’t confirm to expected type Reads[Application.this.IOIS]
  //implicit val ioisReads: Reads[IOIS] = (JsPath \ "iois").read[Seq[IOI]]

  //Expression of type Reads[Application.this.IOI] dosen’t confirm to expected type Reads[Application.this.IOIS]
    /*implicit val ioisReads: Reads[IOIS] = (
      (JsPath \ "iois").read[JsValue].map{
        case arr: JsArray => arr.as[Seq[IOI]]
        case obj: JsObject => Seq(obj.as[IOI])
      }
      )(IOIS.apply _)*/

  // これはnull pointer excepion
  //implicit val ioisReads: Reads[Seq[IOI]] = JsPath.read[Seq[IOI]]

  // StackOverflow
  /*implicit val ioisReads: Reads[Seq[IOI]] = JsPath.read[JsValue].map{
    case arr: JsArray => arr.as[Seq[IOI]]
    case obj: JsObject => Seq(obj.as[IOI])
  }*/




  implicit val placeReads: Reads[Place] = (
    (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "location").read[Location] and
      (JsPath \ "residents").read[Seq[IOI]]
    )(Place.apply _)


  // 2019/11/16 22:20 ためしにこれ消してみる
  implicit val ioiWrites: Writes[IOI] =
    (JsPath \ "symbol").write[String].and((JsPath \ "ioiqty").write[Int])
      .and((JsPath \ "ioiqualifier").write[String])(unlift(IOI.unapply))

  /*implicit val ioisWrites: Writes[IOIS] =
    (JsPath \ "name").write[String].and((JsPath \ "iois").write[Seq[IOI]])(unlift(IOIS.unapply))*/



  //implicit val ioisReads: Reads[List[IOI]] = JsPath.read[List[IOI]]

/* これは比較のためここに書いてあるだけ*/
  /*implicit val residentReads: Reads[Resident] = (
    (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "age").read[Int](min(0) keepAnd max(150)) and
      (JsPath \ "role").readNullable[String]
    )(Resident.apply _)*/



  /*implicit val ioiWrites = new Writes[IOI] {
    def writes(ioi: IOI) = Json.obj(
      "symbol" -> ioi.symbol,
      "ioiqty" -> ioi.ioiqty,
      "ioiqualifier" -> ioi.ioiQualifiers
    )
  }*/

  def savePlace = Action(parse.json) { request =>
    println(request.body)
    val placeResult = request.body.validate[Place]
    placeResult.fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))
      },
      place => {
        Place.save(place)
        Ok(Json.obj("status" -> "OK", "message" -> ("Place '" + place.name + "' saved.")))
      }
    )
  }

  def newIOI = Action(parse.json) { request =>
    println(request.body)
    val newIOIResult = request.body.validate[Seq[IOI]]
    newIOIResult.fold(
      errors => {
        println(errors)
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))
      },
      iois => {
        println(s"iois=$iois")
        val s = Json.prettyPrint(Json.toJson(iois))
        println(s)
        write(s, () => "D:\\tmp\\a.txt")

        Ok(Json.obj("status" -> "OK", "message" -> "abcd"))
      }
    )
  }

  def write(str: String, f: () => String): Path = {
    val path = Paths.get(f())
    Files.write(path, Seq(str).asJava)
  }

  def read(f: () => String): Unit = {
    val path = Paths.get(f())
    Files.readAllLines(path).asScala.toList
  }


}
