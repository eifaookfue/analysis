package jp.co.nri.nefs.tool.analytics.common.property

object EMarket extends Enumeration {
  type EMarket = Value
  val TYO_MAIN, TYO_NGO, TYO_FKA, TYO_SAP, TYO_TOST, JSD_OTC, OSA_DERIV, OTC_MAIN, CHJ_MAIN = Value
  val key = "MARKET"
}
