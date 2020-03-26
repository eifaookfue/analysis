package jp.co.nri.nefs.tool.analytics.common.property

import jp.co.nri.nefs.tool.util.EnumerationLike

object ECrossType extends Enumeration with EnumerationLike  {
  type ECrossType = Value
  val SINGLE, BASKET, FIXED_PRICE, BUY_BACK, DISTRIUBUTION = Value
  val key = "CROSS_TYPE"
}
