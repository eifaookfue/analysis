package jp.co.nri.nefs.tool.log.common.model

import java.sql.Timestamp

case class WindowDetail( appName: String, computerName: String, userId:String, tradeDate: String, lineNo: Long,
                         handler: String, windowName: Option[String], destinationType: Option[String],
                         action: Option[String], method: Option[String],
                         time: Timestamp, startupTime: Long)