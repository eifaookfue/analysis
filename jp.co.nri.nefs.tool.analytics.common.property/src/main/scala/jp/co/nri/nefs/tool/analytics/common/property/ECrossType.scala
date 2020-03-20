package jp.co.nri.nefs.tool.analytics.common.property

object ECrossType extends Enumeration {
  type ECrossType = Value
  val SINGLE, BASKET, FIXED_PRICE, BUY_BACK, DISTRIUBUTION = Value
  val key = "CROSS_TYPE"
}
