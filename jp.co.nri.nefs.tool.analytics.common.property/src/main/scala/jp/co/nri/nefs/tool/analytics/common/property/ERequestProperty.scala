package jp.co.nri.nefs.tool.analytics.common.property

object ERequestProperty extends Enumeration {
  type ERequestProperty = Value
  val ENewInterventionProperty, ENewChildOrderProperty, ENewChildOrderAndAlgoProperty,
  ENewOrderAndAlgoProperty, ENewBlockDetailProperty, ENewBasketCrossProperty = Value

}
