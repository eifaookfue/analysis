package jp.co.nri.nefs.tool.analytics.common.property

object ECounterParty extends Enumeration {
  type ECounterParty = Value
  val BROKER, RETELA = Value
  val key = "CROSS_COUNTERPARTY"
}
