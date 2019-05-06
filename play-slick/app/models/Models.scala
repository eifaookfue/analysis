package models

import java.sql.Timestamp

case class Window(handler: String, dialogName: Option[String], action: Option[String],
                  destinationType: Option[String], userName: String,
                  tradeDate: String, time: Timestamp, startupTime: Long)
