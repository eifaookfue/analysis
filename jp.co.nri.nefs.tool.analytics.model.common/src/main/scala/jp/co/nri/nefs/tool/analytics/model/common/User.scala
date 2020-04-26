package jp.co.nri.nefs.tool.analytics.model.common

import java.sql.Timestamp

case class User(userId: String, userName: String, updateTime: Timestamp = null)
