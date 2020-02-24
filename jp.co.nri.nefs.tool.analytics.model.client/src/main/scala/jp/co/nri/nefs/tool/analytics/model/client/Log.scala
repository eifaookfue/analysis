package jp.co.nri.nefs.tool.analytics.model.client

import java.sql.Timestamp

case class Log( logId: Long, appName: String, computerName: String, userId:String,
                tradeDate: String, time: Timestamp, fileName: String)
