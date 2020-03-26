package jp.co.nri.nefs.tool.analytics.common.property

import jp.co.nri.nefs.tool.util.EnumerationLike

object ECounterParty extends Enumeration with EnumerationLike  {
  type ECounterParty = Value
  val BROKER, RETELA = Value
  val key = "CROSS_COUNTERPARTY"
}
