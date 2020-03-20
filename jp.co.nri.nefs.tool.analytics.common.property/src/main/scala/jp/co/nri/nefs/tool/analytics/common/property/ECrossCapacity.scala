package jp.co.nri.nefs.tool.analytics.common.property

object ECrossCapacity extends Enumeration {
  type ECrossCapacity = Value
  val PRINCIPAL, AGENCY, BROKER = Value
  val key = "CROSS_CAPACITY"
}
