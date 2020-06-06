package jp.co.nri.nefs.tool.analytics.model.client
import java.sql.Timestamp

import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

case class E9n(e9nId: Int, e9nHeadMessage: String, e9nLength: Int, count: Int, updateTime: Timestamp = null)

object E9n {
  implicit val e9nWrites: Writes[E9n] = (
    (JsPath \ "e9n-id").write[Int] and
      (JsPath \ "message").write[String] and
      (JsPath \ "e9n-length").write[Int] and
      (JsPath \ "count").write[Int] and
      (JsPath \ "update-time").write[Timestamp]
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
    } else if (index == 2 && isDesc) {
      e9ns.sortBy(_.count)(Ordering[Int].reverse)
    } else  {
      e9ns.sortBy(_.count)
    }
  }
}

trait E9nComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class E9ns(tag: Tag) extends Table[E9n](tag, "E9N") {
    def e9nId = column[Int]("E9N_ID", O.PrimaryKey, O.AutoInc)
    def e9nHeadMessage = column[String]("E9N_HEAD_MESSAGE", O.Length(200))
    def e9nLength = column[Int]("E9N_LENGTH")
    def count = column[Int]("COUNT")
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    // https://stackoverflow.com/questions/22367092/using-tupled-method-when-companion-object-is-in-class
    def * = (e9nId, e9nHeadMessage, e9nLength, count, updateTime) <> ((E9n.apply _).tupled, E9n.unapply)
    def uk_1 = index("E9N_UK_1", (e9nHeadMessage, e9nLength), unique = true)
  }
}

case class E9nDetail(logId: Int, lineNo: Int, e9nId: Int)

trait E9nDetailComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class E9nDetails(tag: Tag) extends Table[E9nDetail](tag, "E9N_DETAIL") {
    def logId = column[Int]("LOG_ID")
    def lineNo = column[Int]("LINE_NO")
    def e9nId = column[Int]("E9N_ID")
    def * = (logId, lineNo, e9nId) <> (E9nDetail.tupled, E9nDetail.unapply)
    def pk = primaryKey("E9N_PK_1", (logId, lineNo))
  }
}

case class E9nStackTrace (e9nId: Int, number: Int, message: String, updateTime: Timestamp = null)

trait E9nStackTraceComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class E9nStackTraces(tag: Tag) extends Table[E9nStackTrace](tag, "E9N_STACKTRACE") {
    def e9nId = column[Int]("E9N_ID")
    def number = column[Int]("NUMBER")
    def message = column[String]("MESSAGE")
    def updateTime = column[Timestamp]("UPDATE_TIME", SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"))
    def * = (e9nId, number, message, updateTime) <> (E9nStackTrace.tupled, E9nStackTrace.unapply)
    def pk = primaryKey("E9N_STACKTRACE_PK_1", (e9nId, number))
  }
}