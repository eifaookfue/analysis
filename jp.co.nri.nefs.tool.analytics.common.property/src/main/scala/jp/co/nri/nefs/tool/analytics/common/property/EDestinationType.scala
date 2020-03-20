package jp.co.nri.nefs.tool.analytics.common.property

object EDestinationType extends Enumeration {
  type EDestinationType = Value
  val EXCHANGE, CHILD_ORDER, ALGO,
  TOST_PRINCIPAL, TOST_AGENCY, TOST_BROKER, TOST2, TOST3_BUY_BACK, TOST3_BUNBAI,
  OTC_PRINCIPAL, OTC_AGENCY, OTC_BROKER,
  JNET_PRINCIPAL, JNET_BROKER,
  CLOSE_PRICE, BUY_BACK, BUNBAI, OFF_AUCTION,
  ODD_LOT_PRINCIPAL, ODD_LOT_AGENCY,
  WAVE, TRADING_LIST = Value
  val key = "DESTINATION_TYPE"
}
