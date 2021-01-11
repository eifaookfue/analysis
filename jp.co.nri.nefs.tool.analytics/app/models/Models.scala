package models

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import jp.co.nri.nefs.tool.analytics.model.client.STATUS
import play.api.Logging
import play.api.libs.json._
import play.api.libs.functional.syntax._

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
                               e9nIdSearchValue: Option[Int],
                               headerSearchValue: String,
                               countSearchValue: Option[Int],
                               statusSearchValue: STATUS,
                               order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean
                                      )

case class WindowCountTableParams(draw: Int,
                                  col0Data: String, col0Name: String, col0Searchable: Boolean, col0Orderable: Boolean, col0SearchValue: String, col0SearchRegex: Boolean,
                                  col1Data: String, col1Name: String, col1Searchable: Boolean, col1Orderable: Boolean, col1SearchValue: String, col1SearchRegex: Boolean,
                                  col2Data: String, col2Name: String, col2Searchable: Boolean, col2Orderable: Boolean, col2SearchValue: String, col2SearchRegex: Boolean,
                                  order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean)


case class E9nListTableParams(draw: Int,
                                  col0Data: String, col0Name: String, col0Searchable: Boolean, col0Orderable: Boolean, col0SearchValue: String, col0SearchRegex: Boolean,
                                  col1Data: String, col1Name: String, col1Searchable: Boolean, col1Orderable: Boolean, col1SearchValue: String, col1SearchRegex: Boolean,
                                  col2Data: String, col2Name: String, col2Searchable: Boolean, col2Orderable: Boolean, col2SearchValue: String, col2SearchRegex: Boolean,
                                  order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean)

case class PreCheckTblRequestParams(draw: Int,
                                    col0SearchValue: String,
                                    col1SearchValue: String,
                                    col2SearchValue: String,
                                    order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean
                                   )

case class Params(page: Int = 0, orderBy: Option[Int]  = None, logId: Option[Int] = None,
                  appName: Option[String]  = None, computerName: Option[String]  = None, userName:Option[String]  = None, tradeDate: Option[String]  = None, lineNo: Option[Int]  = None,
                  activator: Option[String]  = None, windowName: Option[String]  = None, destinationType: Option[String]  = None,
                  action: Option[String]  = None, method: Option[String]  = None,
                  time: Option[Timestamp]  = None, startupTime: Option[Long]  = None, logFile: Option[String]  = None)


case class WindowDetailTbl(logId: Int, appName: String, userName: String,
                           lineNo: Int, activator: String, windowName: String,
                           time: Timestamp)

object timestampWrites extends Writes[Timestamp] {
  val pattern = "yy/MM/dd HH:mm"
  override def writes(o: Timestamp): JsValue = {
    JsString(DateTimeFormatter.ofPattern(pattern).format(o.toLocalDateTime))
  }
}

object WindowDetailTbl {
  implicit val windowDetailTableWrites: Writes[WindowDetailTbl] = (
    (JsPath \ "log-id").write[Int] and
      (JsPath \ "app-name").write[String] and
      (JsPath \ "user-name").write[String] and
      (JsPath \ "line-no").write[Int] and
      (JsPath \ "activator").write[String] and
      (JsPath \ "window-name").write[String] and
      (JsPath \ "time").write[Timestamp](timestampWrites)
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

case class E9nTbl(e9nId: Int, e9nHeadMessage: String, count: Int, status: STATUS)

object Statuses {
  implicit val statusWrites: Writes[STATUS] = Writes[STATUS]{s => JsString(s.toString)}
}

object E9nTbl {

  /*implicit val statusWrites: Writes[STATUS] = new Writes[STATUS] {
    override def writes(o: STATUS): JsValue = JsString(o.toString)
  }*/
  import Statuses.statusWrites

  implicit val e9nTblWrite: Writes[E9nTbl] = (
    (JsPath \ "e9n-id").write[Int] and
      (JsPath \ "message").write[String] and
      (JsPath \ "count").write[Int] and
      (JsPath \ "status").write[STATUS]
    )(unlift(E9nTbl.unapply))
}

case class E9nTblResponse(draw: Int, recordsTotal: Int, recordsFiltered: Int, data: Seq[E9nTbl])

object E9nTblResponse {
  implicit val e9nListDataWrites: Writes[E9nTblResponse] = (
    (JsPath \ "draw").write[Int] and
      (JsPath \ "recordsTotal").write[Int] and
      (JsPath \ "recordsFiltered").write[Int] and
      (JsPath \ "data").write[Seq[E9nTbl]]
    )(unlift(E9nTblResponse.unapply)
  )
}

case class E9nDetailTbl(e9nId: Int, logId: Int, lineNo: Int, appName: String, userName: String, e9nHeadMessage: String)

object E9nDetailTbl {
  implicit val e9nDetailTableWrites: Writes[E9nDetailTbl] = (
    (JsPath \ "e9n-id").write[Int] and
      (JsPath \ "log-id").write[Int] and
      (JsPath \ "line-no").write[Int] and
      (JsPath \ "app-name").write[String] and
      (JsPath \ "user-name").write[String] and
      (JsPath \ "message").write[String]
  )(unlift(E9nDetailTbl.unapply))
}

case class E9nDetailTblResponse(draw: Int, recordsTotal: Int, recordsFiltered: Int, data: Seq[E9nDetailTbl])

object E9nDetailTblResponse {
  implicit val e9nDetailTableDataWrites: Writes[E9nDetailTblResponse] = (
    (JsPath \ "draw").write[Int] and
      (JsPath \ "recordsTotal").write[Int] and
      (JsPath \ "recordsFiltered").write[Int] and
      (JsPath \ "data").write[Seq[E9nDetailTbl]]
  )(unlift(E9nDetailTblResponse.unapply))
}

case class E9nDetailTblRequestParams(draw: Int,
                                        col0SearchValue: String,
                                        col1SearchValue: String,
                                        col2SearchValue: String,
                                        col3SearchValue: String,
                                        col4SearchValue: String,
                                        col5SearchValue: String,
                                        order0Column: Int, order0Dir: String, start: Int, length: Int, searchValue: String, searchRegex: Boolean
                                    )

case class E9nAuditTbl(e9nId: Int, status: STATUS, comment: Option[String], updatedBy: String, updateTime: Timestamp)

object E9nAuditTbl {
  import Statuses.statusWrites
  implicit val E9nAuditTblWrites: Writes[E9nAuditTbl] = (
    (JsPath \ "e9n-id").write[Int] and
      (JsPath \ "status").write[STATUS] and
      (JsPath \ "comment").writeOptionWithNull[String] and
      (JsPath \ "updated-by").write[String] and
      (JsPath \ "updated-time").write[Timestamp](timestampWrites)
    )(unlift(E9nAuditTbl.unapply))
}

case class E9nAuditTblResponse(draw: Int, recordsTotal: Int, recordsFiltered: Int, data: Seq[E9nAuditTbl])

object E9nAuditTblResponse {
  implicit val E9nAuditTblWrites: Writes[E9nAuditTblResponse] = (
    (JsPath \ "draw").write[Int] and
      (JsPath \ "recordsTotal").write[Int] and
      (JsPath \ "recordsFiltered").write[Int] and
      (JsPath \ "data").write[Seq[E9nAuditTbl]]
    )(unlift(E9nAuditTblResponse.unapply))

}

case class E9nAuditTblRequestParams(draw: Int,
                                    e9nIdSearchValue: Option[Int],
                                    statusSearchValue: Option[STATUS],
                                    commentSearchValue: String,
                                    updatedBySearchValue: String,
                                    updatedTimeSearchValue: Option[Timestamp],
                                    start: Int, length: Int, searchValue: String, searchRegex: Boolean
                                    )

case class PreCheckSummaryTbl(message: String, windowName: String, count: Int)

object PreCheckSummaryTbl {
  implicit val preCheckSummaryTblWrite: Writes[PreCheckSummaryTbl] = (
    (JsPath \ "message").write[String] and
      (JsPath \ "window-name").write[String] and
      (JsPath \ "count").write[Int]
    )(unlift(PreCheckSummaryTbl.unapply))
}

case class PreCheckTblResponse(draw: Int, recordsTotal: Int, recordsFiltered: Int, data: Seq[PreCheckSummaryTbl])

object PreCheckTblResponse {
  implicit val preCheckTblResponseWrite: Writes[PreCheckTblResponse] = (
    (JsPath \ "draw").write[Int] and
      (JsPath \ "recordsTotal").write[Int] and
      (JsPath \ "recordsFiltered").write[Int] and
      (JsPath \ "data").write[Seq[PreCheckSummaryTbl]]
  )(unlift(PreCheckTblResponse.unapply))
}

sealed abstract class Menu(val parent: Menu)

case object DASHBOARD extends Menu(null)
case object DASHBOARD_CLIENT extends Menu(DASHBOARD)
case object DASHBOARD_SERVER extends Menu(DASHBOARD)
case object WINDOW extends Menu(null)
case object NEW_ORDER_SINGLE extends Menu(WINDOW)
case object DETAIL extends Menu(null)
case object WINDOW_DETAIL extends Menu(DETAIL)
case object E9N_DETAIL extends Menu(DETAIL)