package jp.co.nri.nefs.tool.log.common.utils

object RegexUtils {
  val regex = """(.*)_(OMS_.*)_(.*)_([0-9][0-9][0-9][0-9][0-9][0-9])_([0-9]*).log$""".r
  case class OMSAplInfo(appName: String, env: String, computer: String, userId: String, startTime: String){
    val tradeDate: String = startTime.take(8)
  }

  def getOMSAplInfo(fileName: String): Option[OMSAplInfo] = {
    fileName match {
      case regex(appName, env, computer, userId, startTime) => Some(OMSAplInfo(appName, env, computer, userId, startTime))
      case _ => None
    }
  }
}