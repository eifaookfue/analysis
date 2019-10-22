package jp.co.nri.nefs.tool.log.common.model

import java.sql.Timestamp

case class Log( logId: Long, appName: String, computerName: String, userId:String, tradeDate: String)

case class WindowDetail(logId: Long, lineNo: Int,
                handler: String, windowName: Option[String], destinationType: Option[String],
                action: Option[String], method: Option[String],
                time: Timestamp, startupTime: Long)
