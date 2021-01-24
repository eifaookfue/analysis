package jp.co.nri.nefs.tool.analytics.model.client
import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class E9n(e9nId: Int, e9nHeadMessage: String, e9nLength: Int)

case class E9nEx(e9n: E9n, updateTime: Timestamp)

object E9n {
  implicit val e9nWrites: Writes[E9n] = (
    (JsPath \ "e9n-id").write[Int] and
      (JsPath \ "message").write[String] and
      (JsPath \ "e9n-length").write[Int]
    )(unlift(E9n.unapply)
  )

  def sort(e9ns: Seq[E9n], index: Int, dic: String): Seq[E9n] = {
    val isDesc = dic == "desc"

    if (index == 0 && isDesc) {
      e9ns.sortBy(_.e9nId)(Ordering[Int].reverse)
    } else if (index == 0 && !isDesc) {
      e9ns.sortBy(_.e9nId)
    } else if (index == 1 && isDesc) {
      e9ns.sortBy(_.e9nHeadMessage)(Ordering[String].reverse)
    } else if (index == 1 && !isDesc) {
      e9ns.sortBy(_.e9nHeadMessage)
    } else  {
      e9ns.sortBy(_.e9nId)
    }
  }
}

trait E9nComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class E9ns(tag: Tag) extends Table[E9nEx](tag, "E9N") {
    def e9nId = column[Int]("E9N_ID", O.PrimaryKey, O.AutoInc)
    def e9nHeadMessage = column[String]("E9N_HEAD_MESSAGE", O.Length(200))
    def e9nLength = column[Int]("E9N_LENGTH")
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    // https://stackoverflow.com/questions/22367092/using-tupled-method-when-companion-object-is-in-class
    def e9nProjection = (e9nId, e9nHeadMessage, e9nLength) <> ((E9n.apply _).tupled, E9n.unapply)
    def * = (e9nProjection, updateTime) <> ((E9nEx.apply _).tupled, E9nEx.unapply)
    def uk_1 = index("E9N_UK_1", (e9nHeadMessage, e9nLength), unique = true)
  }
}

case class E9nDetail(logId: Int, lineNo: Int, e9nId: Int, time: Timestamp)

case class E9nDetailEx(e9nDetail: E9nDetail, updateTime: Timestamp)

trait E9nDetailComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class E9nDetails(tag: Tag) extends Table[E9nDetailEx](tag, "E9N_DETAIL") {
    def logId = column[Int]("LOG_ID")
    def lineNo = column[Int]("LINE_NO")
    def e9nId = column[Int]("E9N_ID")
    def time = column[Timestamp]("TIME")
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def e9nDetailProjection = (logId, lineNo, e9nId, time) <> (E9nDetail.tupled, E9nDetail.unapply)
    def * = (e9nDetailProjection,
      updateTime) <> (E9nDetailEx.tupled, E9nDetailEx.unapply)
    def pk = primaryKey("E9N_PK_1", (logId, lineNo))
  }
}

case class E9nStackTrace(e9nId: Int, number: Int, message: String)

case class E9nStackTraceEx(e9nStackTrace: E9nStackTrace, updateTime: Timestamp)

trait E9nStackTraceComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class E9nStackTraces(tag: Tag) extends Table[E9nStackTraceEx](tag, "E9N_STACKTRACE") {
    def e9nId = column[Int]("E9N_ID")
    def number = column[Int]("NUMBER")
    def message = column[String]("MESSAGE")
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def e9nStackTraceProjection = (e9nId, number, message) <> (E9nStackTrace.tupled, E9nStackTrace.unapply)
    def * = (e9nStackTraceProjection, updateTime) <> (E9nStackTraceEx.tupled, E9nStackTraceEx.unapply)
    def pk = primaryKey("E9N_STACKTRACE_PK_1", (e9nId, number))
  }
}

case class E9nAudit(e9nId: Int, status: STATUS, comment: Option[String], updatedBy: String)
case class E9nAuditEx(audit: E9nAudit, updateTime: Timestamp)

sealed trait STATUS
object STATUS {

  case object NOT_YET extends STATUS
  case object DONE extends STATUS
  case object PENDING extends STATUS

  val defaultStatus: STATUS = NOT_YET

  def valueOf(name: String): STATUS = {
    name match {
      case "NOT_YET" => NOT_YET
      case "DONE" => DONE
      case "PENDING" => PENDING
      case _ => throw new IllegalArgumentException(s"$name is not a member of STATUS")
    }
  }

  val values: Seq[STATUS] = Seq(NOT_YET, DONE, PENDING)
}

trait E9nAuditCommonComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  implicit val statusType: BaseColumnType[STATUS] = MappedColumnType.base[STATUS, String](
    _.toString,
    STATUS.valueOf
  )

}

trait E9nAuditComponent extends E9nAuditCommonComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class E9nAudits(tag: Tag) extends Table[E9nAuditEx](tag, "E9N_AUDIT") {
    def e9nId = column[Int]("E9N_ID", O.PrimaryKey)
    def status = column[STATUS]("STATUS")
    def comment = column[Option[String]]("COMMENT")
    def updatedBy = column[String]("UPDATED_BY")
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def auditProjection = (e9nId, status, comment, updatedBy) <> (E9nAudit.tupled, E9nAudit.unapply)
    def * = (auditProjection, updateTime) <> (E9nAuditEx.tupled, E9nAuditEx.unapply)
  }
}

case class E9nAuditHistory(e9nHistoryId: Int, e9nId: Int, status: Option[STATUS],
                           comment: Option[String], updatedBy: String)

case class E9nAuditHistoryEx(e9nAuditHistory: E9nAuditHistory, updateTime: Timestamp)

trait E9nAuditHistoryComponent extends E9nAuditCommonComponent {

  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class E9nAuditHistories(tag: Tag) extends Table[E9nAuditHistoryEx](tag, "E9N_AUDIT_HISTORY") {
    def e9nHistoryId = column[Int]("E9N_HISTORY_ID", O.PrimaryKey, O.AutoInc)
    def e9nId = column[Int]("E9N_ID")
    def status = column[Option[STATUS]]("STATUS")
    def comment = column[Option[String]]("COMMENT")
    def updatedBy = column[String]("UPDATED_BY")
    def e9nAuditHistoryProjection = (e9nHistoryId, e9nId, status, comment, updatedBy) <> (E9nAuditHistory.tupled, E9nAuditHistory.unapply)
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def * = (e9nAuditHistoryProjection, updateTime) <> (E9nAuditHistoryEx.tupled, E9nAuditHistoryEx.unapply)

  }
}

case class E9nCount(e9nId: Int, count: Int, updateTime: Timestamp = null)

trait E9nCountComponent {

  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class E9nCounts(tag: Tag) extends Table[E9nCount](tag, "E9N_COUNT") {

    def e9nId = column[Int]("E9N_ID", O.PrimaryKey)
    def count = column[Int]("COUNT")
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def * = (e9nId, count, updateTime) <> (E9nCount.tupled, E9nCount.unapply)

  }

}