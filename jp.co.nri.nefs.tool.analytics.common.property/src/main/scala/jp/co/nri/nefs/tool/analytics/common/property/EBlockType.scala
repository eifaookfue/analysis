package jp.co.nri.nefs.tool.analytics.common.property

import jp.co.nri.nefs.tool.util.EnumerationLike

object EBlockType extends Enumeration with EnumerationLike  {
  type EBlockType = Value
  val WAVE, TRADING_LIST = Value
  val key = "BLOCK_TYPE"
}
