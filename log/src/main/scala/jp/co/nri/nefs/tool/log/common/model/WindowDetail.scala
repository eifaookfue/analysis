package jp.co.nri.nefs.tool.log.common.model

import java.sql.Timestamp

case class Log( logId: Long, appName: String, computerName: String, userId:String,
                tradeDate: String, time: Timestamp)

case class WindowDetail(logId: Long, lineNo: Int,
                        activator: Option[String], windowName: Option[String], destinationType: Option[String],
                action: Option[String], method: Option[String],
                time: Timestamp, startupTime: Option[Long])

case class User(userId: String, userName: String)
