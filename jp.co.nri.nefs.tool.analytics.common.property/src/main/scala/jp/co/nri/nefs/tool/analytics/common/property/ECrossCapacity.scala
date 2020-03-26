package jp.co.nri.nefs.tool.analytics.common.property

import jp.co.nri.nefs.tool.util.EnumerationLike

object ECrossCapacity extends Enumeration with EnumerationLike {
  type ECrossCapacity = Value
  val PRINCIPAL, AGENCY, BROKER = Value
  val key = "CROSS_CAPACITY"
}
