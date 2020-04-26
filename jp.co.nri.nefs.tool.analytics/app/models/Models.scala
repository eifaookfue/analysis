package models

import java.sql.Timestamp

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.QueryStringBindable

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}


case class Params(page: Int = 0, orderBy: Option[Int]  = None, logId: Option[Int] = None,
                  appName: Option[String]  = None, computerName: Option[String]  = None, userName:Option[String]  = None, tradeDate: Option[String]  = None, lineNo: Option[Long]  = None,
                  activator: Option[String]  = None, windowName: Option[String]  = None, destinationType: Option[String]  = None,
                  action: Option[String]  = None, method: Option[String]  = None,
                  time: Option[Timestamp]  = None, startupTime: Option[Long]  = None, logFile: Option[String]  = None)


object Params {
  implicit def queryStringBindable(implicit intBinder: QueryStringBindable[Int],
                                   strBinder: QueryStringBindable[String],
                                   longBinder: QueryStringBindable[Long]): QueryStringBindable[Params] = new QueryStringBindable[Params] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Params]] = {
      val page = intBinder.bind("page", params)
      val orderBy = intBinder.bind("orderBy", params)
      val appName = strBinder.bind("appName", params)
      val logId = intBinder.bind("logId", params)
      val computerName = strBinder.bind("computerName", params)
      val userId = strBinder.bind("userId", params)
      val tradeDate = strBinder.bind("tradeDate", params)
      val lineNo = intBinder.bind("lineNo", params)
      val handler = strBinder.bind("handler", params)
      val windowName = strBinder.bind("windowName", params)
      val destinationType = strBinder.bind("destinationType", params)
      val action = strBinder.bind("action", params)
      val method = strBinder.bind("action", params)
      val time = longBinder.bind("time", params)
      val startupTime = longBinder.bind("startupTime", params)
      val logFile = strBinder.bind("logFile", params)
      Some(Right(Params(page.map(_.right.get).getOrElse(0),orderBy.map(_.right.get),
        logId.map(_.right.get),
        appName.map(_.right.get),computerName.map(_.right.get), userId.map(_.right.get),
        tradeDate.map(_.right.get), lineNo.map(_.right.get), handler.map(_.right.get),
        windowName.map(_.right.get), destinationType.map(_.right.get), action.map(_.right.get),
        method.map(_.right.get), time.map(e => new Timestamp(e.right.get)),
        startupTime.map(_.right.get), logFile.map(_.right.get))))
    }
    override def unbind(key: String, params: Params): String = {
      List(Some(intBinder.unbind("page", params.page)), params.orderBy.map(intBinder.unbind("orderBy",_)),
        params.logId.map(longBinder.unbind("logId",_)),
        params.appName.map(strBinder.unbind("appName",_)), params.computerName.map(strBinder.unbind("computerName",_)),
        params.userName.map(strBinder.unbind("userName",_)), params.tradeDate.map(strBinder.unbind("tradeDate",_)),
        params.lineNo.map(longBinder.unbind("lineNo",_)), params.activator.map(strBinder.unbind("activator",_)),
        params.windowName.map(strBinder.unbind("windowName",_)), params.destinationType.map(strBinder.unbind("destinationType",_)),
        params.action.map(strBinder.unbind("action",_)), params.method.map(strBinder.unbind("method",_)),
        params.time.map(t => longBinder.unbind("time",t.getTime)), params.startupTime.map(longBinder.unbind("startupTime",_)),
        params.logFile.map(strBinder.unbind("logFile",_))
      ).collect({ case Some(p) => p}).reduceLeft(_ + "&" + _)
    }
  }
}

case class WindowCountBySlice(slice: String, newOrderSingleCount: Int, newSliceCount: Int,
                              otherCount: Int)

object WindowCountBySlice {
  implicit val windowCountBySliceWrites: Writes[WindowCountBySlice] = (
    (JsPath \ "slice").write[String] and
      (JsPath \ "NewOrderSingle").write[Int] and
      (JsPath \ "NewSlice").write[Int] and
      (JsPath \ "Other").write[Int]
  )(unlift(WindowCountBySlice.unapply))
}

case class WindowCountByDate(tradeDate: String, newOrderSingleCount: Int, newSliceCount: Int, otherCount: Int)

object WindowCountByDate {
  implicit val windowCountByDateWrites: Writes[WindowCountByDate] = (
    (JsPath \ "trade_date").write[String] and
      (JsPath \ "NewOrderSingle").write[Int] and
      (JsPath \ "NewSlice").write[Int] and
      (JsPath \ "Other").write[Int]
    )(unlift(WindowCountByDate.unapply))
}

case class WindowCountByUser(userName: String, windowName: String, count: Int)

object WindowCountByUser {
  implicit val windowCountByUserWrites: Writes[WindowCountByUser] = (
    (JsPath \ "user_name").write[String] and
      (JsPath \ "window_name").write[String] and
      (JsPath \ "count").write[Int]
    )(unlift(WindowCountByUser.unapply)
  )
}

sealed abstract class Menu(val parent: Menu)

case object DASHBOARD extends Menu(null)
case object DASHBOARD_CLIENT extends Menu(DASHBOARD)
case object DASHBOARD_SERVER extends Menu(DASHBOARD)
case object WINDOW extends Menu(null)
case object NEW_ORDER_SINGLE extends Menu(WINDOW)