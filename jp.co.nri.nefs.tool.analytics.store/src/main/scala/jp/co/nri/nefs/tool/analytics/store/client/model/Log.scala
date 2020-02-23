package jp.co.nri.nefs.tool.analytics.store.client.model

import java.sql.Timestamp

case class Log( logId: Long, appName: String, computerName: String, userId:String,
                tradeDate: String, time: Timestamp, fileName: String)
