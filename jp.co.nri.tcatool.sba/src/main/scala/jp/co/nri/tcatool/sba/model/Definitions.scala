package jp.co.nri.tcatool.sba.model

trait EHistoryType
object EHistoryType {
  case object NEW extends EHistoryType
  case object AMEND extends EHistoryType
  case object CANCEL extends EHistoryType

  def valueOf(str: String): EHistoryType = {
    str match {
      case "0" => NEW
      case "1" => AMEND
      case "2" => CANCEL
      case _ => throw new IllegalArgumentException(s"Couldn't convert $str to EHistoryType")
    }
  }
}

trait EOrderType
object EOrderType {
  case object OB extends EOrderType
  case object Care extends EOrderType

  def valueOf(str: String): EOrderType = {
    str match {
      case "2" => OB
      case "3" => Care
      case _ => throw new IllegalArgumentException(s"Couldn't convert $str to EOrderType")
    }
  }

}

