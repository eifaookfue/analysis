package models

import java.sql.Timestamp

import jp.co.nri.nefs.tool.analytics.model.client.E9n
import play.api.Logging
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.QueryStringBindable

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class WindowDetailTblRequestParams(draw: Int,
                                        col0SearchValue: String,
                                        col1SearchValue: String,
                                        col2SearchValue: String,
                                        col3SearchValue: String,
                                        col4SearchValue: String,
                                        col5SearchValue: String,
                                        col6SearchValue: String,
                                        order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean
                                  )

case class WindowSliceTblRequestParams(draw: Int,
                                        col0SearchValue: String,
                                        col1SearchValue: String,
                                        col2SearchValue: String,
                                        order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean
                                       )

case class E9nTblRequestParams(draw: Int,
                                       col0SearchValue: String,
                                       col1SearchValue: String,
                                       col2SearchValue: String,
                                       order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean
                                      )

case class WindowCountTableParams(draw: Int,
                                  col0Data: String, col0Name: String, col0Searchable: Boolean, col0Orderable: Boolean, col0SearchValue: String, col0SearchRegex: Boolean,
                                  col1Data: String, col1Name: String, col1Searchable: Boolean, col1Orderable: Boolean, col1SearchValue: String, col1SearchRegex: Boolean,
                                  col2Data: String, col2Name: String, col2Searchable: Boolean, col2Orderable: Boolean, col2SearchValue: String, col2SearchRegex: Boolean,
                                  order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean)

object WindowCountTableParams {
  implicit def queryStringBindable(implicit intBinder: QueryStringBindable[Int],
                                   strBinder: QueryStringBindable[String],
                                   boolBinder: QueryStringBindable[Boolean]): QueryStringBindable[WindowCountTableParams] = new QueryStringBindable[WindowCountTableParams] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, WindowCountTableParams]] = {
      val draw = intBinder.bind("draw", params)
      val col0Data = strBinder.bind("columns[0][data]", params)
      val col0Name = strBinder.bind("columns[0][name]", params)
      val col0Searchable = boolBinder.bind("columns[0][searchable]", params)
      val col0Orderable = boolBinder.bind("columns[0][orderable]", params)
      val col0SearchValue = strBinder.bind("columns[0][search][value]", params)
      val col0SearchRegex = boolBinder.bind("columns[0][search][regex]", params)
      val col1Data = strBinder.bind("columns[1][data]", params)
      val col1Name = strBinder.bind("columns[1][name]", params)
      val col1Searchable = boolBinder.bind("columns[1][searchable]", params)
      val col1Orderable = boolBinder.bind("columns[1][orderable]", params)
      val col1SearchValue = strBinder.bind("columns[1][search][value]", params)
      val col1SearchRegex = boolBinder.bind("columns[1][search][regex]", params)
      val col2Data = strBinder.bind("columns[2][data]", params)
      val col2Name = strBinder.bind("columns[2][name]", params)
      val col2Searchable = boolBinder.bind("columns[2][searchable]", params)
      val col2Orderable = boolBinder.bind("columns[2][orderable]", params)
      val col2SearchValue = strBinder.bind("columns[2][search][value]", params)
      val col2SearchRegex = boolBinder.bind("columns[2][search][regex]", params)
      val order0Column = intBinder.bind("order[0][column]", params)
      val order0Dir = strBinder.bind("order[0][dir]", params)
      val start = intBinder.bind("start", params)
      val length = intBinder.bind("length", params)
      val searchValue = strBinder.bind("search[value]", params)
      val searchRegex = boolBinder.bind("search[regex]", params)
      println(s"bind called. draw=$draw")
      Some(Right(WindowCountTableParams(draw.get.right.get,
        col0Data.get.right.get, col0Name.get.right.get, col0Searchable.get.right.get, col0Orderable.get.right.get, col0SearchValue.get.right.get, col0SearchRegex.get.right.get,
        col1Data.get.right.get, col1Name.get.right.get, col1Searchable.get.right.get, col1Orderable.get.right.get, col1SearchValue.get.right.get, col1SearchRegex.get.right.get,
        col2Data.get.right.get, col2Name.get.right.get, col2Searchable.get.right.get, col2Orderable.get.right.get, col2SearchValue.get.right.get, col2SearchRegex.get.right.get,
        order0Column.get.right.get, order0Dir.get.right.get, start.get.right.get, length.get.right.get, searchValue.get.right.get, searchRegex.get.right.get
      )))
    }

    override def unbind(key: String, value: WindowCountTableParams): String = {
      List(intBinder.unbind("draw", value.draw), strBinder.unbind("columns[0][data]", value.col0Data), strBinder.unbind("columns[0][name]", value.col0Name),
        boolBinder.unbind("columns[0][searchable]", value.col0Searchable), boolBinder.unbind("columns[0][orderable]", value.col0Orderable),
        strBinder.unbind("columns[0][search][value]", value.col0SearchValue), boolBinder.unbind("columns[0][search][regex]", value.col0SearchRegex),
        strBinder.unbind("columns[1][data]", value.col1Data), strBinder.unbind("columns[1][name]", value.col1Name),
        boolBinder.unbind("columns[1][searchable]", value.col1Searchable), boolBinder.unbind("columns[1][orderable]", value.col1Orderable),
        strBinder.unbind("columns[1][search][value]", value.col1SearchValue), boolBinder.unbind("columns[1][search][regex]", value.col1SearchRegex),
        strBinder.unbind("columns[2][data]", value.col2Data), strBinder.unbind("columns[2][name]", value.col2Name),
        boolBinder.unbind("columns[2][searchable]", value.col2Searchable), boolBinder.unbind("columns[2][orderable]", value.col2Orderable),
        strBinder.unbind("columns[2][search][value]", value.col2SearchValue), boolBinder.unbind("columns[2][search][regex]", value.col2SearchRegex),
        intBinder.unbind("order[0][column]", value.order0Column), strBinder.unbind("order[0][dir]", value.order0Dir),
        intBinder.unbind("start", value.start), intBinder.unbind("length", value.length),
        strBinder.unbind("search[value]", value.searchValue), boolBinder.unbind("search[regex]", value.searchRegex)
      ).mkString("&")
    }
  }

}

case class E9nListTableParams(draw: Int,
                                  col0Data: String, col0Name: String, col0Searchable: Boolean, col0Orderable: Boolean, col0SearchValue: String, col0SearchRegex: Boolean,
                                  col1Data: String, col1Name: String, col1Searchable: Boolean, col1Orderable: Boolean, col1SearchValue: String, col1SearchRegex: Boolean,
                                  col2Data: String, col2Name: String, col2Searchable: Boolean, col2Orderable: Boolean, col2SearchValue: String, col2SearchRegex: Boolean,
                                  order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean)

object E9nListTableParams {
  implicit def queryStringBindable(implicit intBinder: QueryStringBindable[Int],
                                   strBinder: QueryStringBindable[String],
                                   boolBinder: QueryStringBindable[Boolean]): QueryStringBindable[E9nListTableParams] = new QueryStringBindable[E9nListTableParams] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, E9nListTableParams]] = {
      val draw = intBinder.bind("draw", params)
      val col0Data = strBinder.bind("columns[0][data]", params)
      val col0Name = strBinder.bind("columns[0][name]", params)
      val col0Searchable = boolBinder.bind("columns[0][searchable]", params)
      val col0Orderable = boolBinder.bind("columns[0][orderable]", params)
      val col0SearchValue = strBinder.bind("columns[0][search][value]", params)
      val col0SearchRegex = boolBinder.bind("columns[0][search][regex]", params)
      val col1Data = strBinder.bind("columns[1][data]", params)
      val col1Name = strBinder.bind("columns[1][name]", params)
      val col1Searchable = boolBinder.bind("columns[1][searchable]", params)
      val col1Orderable = boolBinder.bind("columns[1][orderable]", params)
      val col1SearchValue = strBinder.bind("columns[1][search][value]", params)
      val col1SearchRegex = boolBinder.bind("columns[1][search][regex]", params)
      val col2Data = strBinder.bind("columns[2][data]", params)
      val col2Name = strBinder.bind("columns[2][name]", params)
      val col2Searchable = boolBinder.bind("columns[2][searchable]", params)
      val col2Orderable = boolBinder.bind("columns[2][orderable]", params)
      val col2SearchValue = strBinder.bind("columns[2][search][value]", params)
      val col2SearchRegex = boolBinder.bind("columns[2][search][regex]", params)
      val order0Column = intBinder.bind("order[0][column]", params)
      val order0Dir = strBinder.bind("order[0][dir]", params)
      val start = intBinder.bind("start", params)
      val length = intBinder.bind("length", params)
      val searchValue = strBinder.bind("search[value]", params)
      val searchRegex = boolBinder.bind("search[regex]", params)
      Some(Right(E9nListTableParams(draw.get.right.get,
        col0Data.get.right.get, col0Name.get.right.get, col0Searchable.get.right.get, col0Orderable.get.right.get, col0SearchValue.get.right.get, col0SearchRegex.get.right.get,
        col1Data.get.right.get, col1Name.get.right.get, col1Searchable.get.right.get, col1Orderable.get.right.get, col1SearchValue.get.right.get, col1SearchRegex.get.right.get,
        col2Data.get.right.get, col2Name.get.right.get, col2Searchable.get.right.get, col2Orderable.get.right.get, col2SearchValue.get.right.get, col2SearchRegex.get.right.get,
        order0Column.get.right.get, order0Dir.get.right.get, start.get.right.get, length.get.right.get, searchValue.get.right.get, searchRegex.get.right.get
      )))
    }

    override def unbind(key: String, value: E9nListTableParams): String = {
      List(intBinder.unbind("draw", value.draw), strBinder.unbind("columns[0][data]", value.col0Data), strBinder.unbind("columns[0][name]", value.col0Name),
        boolBinder.unbind("columns[0][searchable]", value.col0Searchable), boolBinder.unbind("columns[0][orderable]", value.col0Orderable),
        strBinder.unbind("columns[0][search][value]", value.col0SearchValue), boolBinder.unbind("columns[0][search][regex]", value.col0SearchRegex),
        strBinder.unbind("columns[1][data]", value.col1Data), strBinder.unbind("columns[1][name]", value.col1Name),
        boolBinder.unbind("columns[1][searchable]", value.col1Searchable), boolBinder.unbind("columns[1][orderable]", value.col1Orderable),
        strBinder.unbind("columns[1][search][value]", value.col1SearchValue), boolBinder.unbind("columns[1][search][regex]", value.col1SearchRegex),
        strBinder.unbind("columns[2][data]", value.col2Data), strBinder.unbind("columns[2][name]", value.col2Name),
        boolBinder.unbind("columns[2][searchable]", value.col2Searchable), boolBinder.unbind("columns[2][orderable]", value.col2Orderable),
        strBinder.unbind("columns[2][search][value]", value.col2SearchValue), boolBinder.unbind("columns[2][search][regex]", value.col2SearchRegex),
        intBinder.unbind("order[0][column]", value.order0Column), strBinder.unbind("order[0][dir]", value.order0Dir),
        intBinder.unbind("start", value.start), intBinder.unbind("length", value.length),
        strBinder.unbind("search[value]", value.searchValue), boolBinder.unbind("search[regex]", value.searchRegex)
      ).mkString("&")
    }
  }

}


case class Params(page: Int = 0, orderBy: Option[Int]  = None, logId: Option[Int] = None,
                  appName: Option[String]  = None, computerName: Option[String]  = None, userName:Option[String]  = None, tradeDate: Option[String]  = None, lineNo: Option[Int]  = None,
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

case class WindowDetailTbl(logId: Int, appName: String, computerName: String, userName: String, tradeDate: String,
                           lineNo: Int, activator: String, windowName: String, destinationType: String, action: String,
                           method: String, time: String, startupTime: Option[Long])

object WindowDetailTbl {
  implicit val windowDetailTableWrites: Writes[WindowDetailTbl] = (
    (JsPath \ "log-id").write[Int] and
      (JsPath \ "app-name").write[String] and
      (JsPath \ "computer-name").write[String] and
      (JsPath \ "user-name").write[String] and
      (JsPath \ "trade-date").write[String] and
      (JsPath \ "line-no").write[Int] and
      (JsPath \ "activator").write[String] and
      (JsPath \ "window-name").write[String] and
      (JsPath \ "destination-type").write[String] and
      (JsPath \ "action").write[String] and
      (JsPath \ "method").write[String] and
      (JsPath \ "time").write[String] and
      (JsPath \ "startup-time").writeNullable[Long]
    )(unlift(WindowDetailTbl.unapply))
}

case class WindowDetailTblResponse(draw: Int, recordsTotal: Int, recordsFiltered: Int, data: Seq[WindowDetailTbl])

object WindowDetailTblResponse {
  implicit val windowDetailTableDataWrites: Writes[WindowDetailTblResponse] = (
    (JsPath \ "draw").write[Int] and
      (JsPath \ "recordsTotal").write[Int] and
      (JsPath \ "recordsFiltered").write[Int] and
      (JsPath \ "data").write[Seq[WindowDetailTbl]]
    )(unlift(WindowDetailTblResponse.unapply))
}


case class WindowCountBySlice(slice: String, newOrderSingleCount: Int, newSliceCount: Int,
                              otherCount: Int)

object WindowCountBySlice {
  implicit val windowCountBySliceWrites: Writes[WindowCountBySlice] = (
    (JsPath \ "Slice").write[String] and
      (JsPath \ "NewOrder").write[Int] and
      (JsPath \ "NewSplit").write[Int] and
      (JsPath \ "Other").write[Int]
  )(unlift(WindowCountBySlice.unapply))
}

case class WindowCountByDate(tradeDate: String, newOrderSingleCount: Int, newSliceCount: Int, otherCount: Int)

object WindowCountByDate {
  implicit val windowCountByDateWrites: Writes[WindowCountByDate] = (
    (JsPath \ "TradeDate").write[String] and
      (JsPath \ "NewOrder").write[Int] and
      (JsPath \ "NewSplit").write[Int] and
      (JsPath \ "Other").write[Int]
    )(unlift(WindowCountByDate.unapply))
}

case class WindowCountByUser(userName: String, windowName: String, count: Int)

object WindowCountByUser extends Logging {
  implicit val windowCountByUserWrites: Writes[WindowCountByUser] = (
    (JsPath \ "user_name").write[String] and
      (JsPath \ "window_name").write[String] and
      (JsPath \ "count").write[Int]
    )(unlift(WindowCountByUser.unapply)
  )
  def sort(users: Seq[WindowCountByUser], index: Int, dic: String): Seq[WindowCountByUser] = {
    val isDesc = dic == "desc"

    if (index == 0 && isDesc) {
      users.sortBy(u => u.userName)(Ordering[String].reverse)
    } else if (index == 0 && !isDesc) {
      users.sortBy(u => u.userName)
    } else if (index == 1 && isDesc) {
      users.sortBy(u => u.windowName)(Ordering[String].reverse)
    } else if (index == 1 && !isDesc) {
      users.sortBy(u => u.windowName)
    } else if (index == 2 && isDesc) {
      users.sortBy(u => u.count)(Ordering[Int].reverse)
    } else  {
      users.sortBy(u => u.count)
    }
  }
}

case class WindowCountByUserData(draw: Int, recordsTotal: Int, recordsFiltered: Int, data: Seq[WindowCountByUser])

object WindowCountByUserData {
  implicit val windowCountByUserDataWrites: Writes[WindowCountByUserData] = (
    (JsPath \ "draw").write[Int] and
      (JsPath \ "recordsTotal").write[Int] and
      (JsPath \ "recordsFiltered").write[Int] and
      (JsPath \ "data").write[Seq[WindowCountByUser]]
    )(unlift(WindowCountByUserData.unapply)
  )
}

case class E9nTblResponse(draw: Int, recordsTotal: Int, recordsFiltered: Int, data: Seq[E9n])

object E9nTblResponse {
  implicit val e9nListDataWrites: Writes[E9nTblResponse] = (
    (JsPath \ "draw").write[Int] and
      (JsPath \ "recordsTotal").write[Int] and
      (JsPath \ "recordsFiltered").write[Int] and
      (JsPath \ "data").write[Seq[E9n]]
    )(unlift(E9nTblResponse.unapply)
  )
}

sealed abstract class Menu(val parent: Menu)

case object DASHBOARD extends Menu(null)
case object DASHBOARD_CLIENT extends Menu(DASHBOARD)
case object DASHBOARD_SERVER extends Menu(DASHBOARD)
case object WINDOW extends Menu(null)
case object NEW_ORDER_SINGLE extends Menu(WINDOW)
case object DETAIL extends Menu(null)
case object WINDOW_DETAIL extends Menu(DETAIL)