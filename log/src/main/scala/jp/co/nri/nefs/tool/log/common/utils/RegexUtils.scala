package jp.co.nri.nefs.tool.log.common.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat

object RegexUtils {
  val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).log$""".r
  val format = new SimpleDateFormat("yyyyMMddHHmmssSSS")
  case class OMSAplInfo(appName: String, env: String, computer: String, userId: String, startTime: String){
    val tradeDate: String = startTime.take(8)
    val time: Timestamp = new Timestamp(format.parse(startTime).getTime)
  }

  def getOMSAplInfo(fileName: String): Option[OMSAplInfo] = {
    fileName match {
      case regex(appName, env, computer, userId, startTime) =>
        Some(OMSAplInfo(appName, env, computer, userId, startTime))
      case _ => None
    }
  }
}