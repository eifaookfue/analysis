package jp.co.nri.nefs.tool.analytics.common.property

import jp.co.nri.nefs.tool.util.EnumerationLike

object EMarket extends Enumeration with EnumerationLike {
  type EMarket = Value
  val TYO_MAIN, TYO_TOST, OSA_DERIV, TYO_NGO, NGO_NNET, TYO_FKA, FKA_OTC, TYO_SAP, SAP_OTC,
  CHJ_MAIN, CHJ_MATCH, OTC_MAIN, JNX_MAIN, IJC_MAIN, JSD_OTC, IMK_MAIN, CHJ_KAIX  = Value
  val key = "MARKET"
}
