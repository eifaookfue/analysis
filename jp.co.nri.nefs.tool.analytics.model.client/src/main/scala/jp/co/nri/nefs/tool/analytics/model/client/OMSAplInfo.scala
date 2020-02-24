
package jp.co.nri.nefs.tool.analytics.model.client

import java.sql.Timestamp
import java.text.SimpleDateFormat

import scala.util.matching.Regex

case class OMSAplInfo(fileName: String, appName: String, env: String, computer: String, userId: String, startTime: String){
  val tradeDate: String = startTime.take(8)
  private val format = new SimpleDateFormat("yyyyMMddHHmmssSSS")
  val time: Timestamp = new Timestamp(format.parse(startTime).getTime)
}

object OMSAplInfo {
  // 以前のログはenvがファイル名に無かった。暫定対応
  val regex1: Regex = """(.*)_(OMS_TKY_OA|OMS_TKY|OMS_HK|OMS_LDN|OMS_NY)_(.*)_([0-9]{6})_([0-9]{17})(_mask.log|.log|_mask.zip|.zip)$""".r
  val regex2: Regex = """(.*)_(.*)_([0-9]{6})_([0-9]{17})(_mask.log|.log|_mask.zip|.zip)$""".r

  def valueOf(fileName: String): Option[OMSAplInfo] = {
    fileName match {
      case regex1(appName, env, computer, userId, startTime, _) => Some(OMSAplInfo(fileName, appName, env, computer, userId, startTime))
      case regex2(appName, computer, userId, startTime, _) => Some(OMSAplInfo(fileName, appName, null, computer, userId, startTime))
      case _ => None
    }
  }


}