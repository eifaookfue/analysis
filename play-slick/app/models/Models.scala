package models

import java.sql.Timestamp

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class WindowDetail(handler: String, windowName: Option[String], destinationType: Option[String],
                        action: Option[String], method: Option[String],
                        userName: String,
                        tradeDate: String, time: Timestamp, startupTime: Long)
