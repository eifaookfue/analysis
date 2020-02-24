package jp.co.nri.nefs.tool.analytics.model.client

import java.sql.Timestamp

case class WindowDetail(logId: Long, lineNo: Int,
                        activator: Option[String], windowName: Option[String], destinationType: Option[String],
                action: Option[String], method: Option[String],
                time: Timestamp, startupTime: Option[Long])
