package jp.co.nri.tcatool.ref.reference.model


sealed abstract class ESessionType(val number: Int) extends Ordered[ESessionType] {
  override def compare(that: ESessionType): Int = number - that.number
}
object ESessionType {
  case object NONE extends ESessionType(0)
  case object AM_PRE_OPEN extends ESessionType(1)
  case object AM_OPEN extends ESessionType(2)
  case object AM_ZARABA extends ESessionType(3)
  case object PM_PRE_OPEN extends ESessionType(4)
  case object PM_OPEN extends ESessionType(5)
  case object PM_ZARABA extends ESessionType(6)
  case object ALL_DAY extends ESessionType(7)

  def valueOf(str: String): ESessionType = {
    str match {
      case "NONE" => NONE
      case "AM_PRE_OPEN" => AM_PRE_OPEN
      case "AM_OPEN" => AM_OPEN
      case "AM_ZARABA" => AM_ZARABA
      case "PM_PRE_OPEN" => PM_PRE_OPEN
      case "PM_OPEN" => PM_OPEN
      case "PM_ZARABA" => PM_ZARABA
      case "ALL_DAY" => ALL_DAY
    }
  }

}

sealed trait EBSType
object EBSType {
  case object B extends EBSType
  case object S extends EBSType

  def valueOf(str: String): EBSType = {
    str match {
      case "B" => B
      case "S" => S
      case _ => throw new NoSuchElementException(s"Couldn't convert $str to EBSType")
    }
  }
}

sealed trait EMarket
object EMarket {
  case object TSE extends EMarket
  case object OSA extends EMarket
  case object NGO extends EMarket
  case object KYO extends EMarket
  case object FKA extends EMarket
  case object SAP extends EMarket
  case object JDQ extends EMarket
  case object NDQ extends EMarket
  case object CHJ extends EMarket
  case object JNX extends EMarket
  case object PTS extends EMarket // Temporary
  case object DKPL extends EMarket

  def valueOf(str: String): EMarket = {
    str match {
      case "TSE" => TSE
      case "OSA" => OSA
      case "NGO" => NGO
      case "KYO" => KYO
      case "FKA" => FKA
      case "SAP" => SAP
      case "JDQ" => JDQ
      case "NDQ" => NDQ
      case "CHJ" => CHJ
      case "JNX" => JNX
      case "PTS" => PTS
      case "DKPL" => DKPL
      case _ => throw new IllegalArgumentException(s"Couldn't convert $str to EMarket")
    }
  }
}

trait EActiveFlag
object EActiveFlag {
  case object ACTIVE extends EActiveFlag
  case object INACTIVE extends EActiveFlag

  def valueOf(str: String): EActiveFlag = {
    str match {
      case "ACTIVE" => ACTIVE
      case "INACTIVE" => INACTIVE
      case _ => throw new IllegalArgumentException(s"Couldn't convert $str to EActiveFlag")
    }
  }
}

trait EProductType
object EProductType {

  case object PRIMARY_DOMESTIC extends EProductType

  case object PRIMARY_FOREIGN extends EProductType

  case object SECONDARY_DOMESTIC extends EProductType

  case object SECONDARY_FOREIGN extends EProductType

  case object MOTHERS_DOMESTIC extends EProductType

  case object MOTHERS_FOREIGN extends EProductType

  case object JDQ_GROWTH_DOMESTIC extends EProductType

  case object JDQ_STANDARD_DOMESTIC extends EProductType

  case object JDQ_STANDARD_FOREIGN extends EProductType

  case object PRO extends EProductType

  case object ETF extends EProductType

  case object REIT extends EProductType

  case object EQUITY_SECURITIES extends EProductType


  def valueOf(str: String): EProductType = {
    str match {
      case "PRIMARY_DOMESTIC" => PRIMARY_DOMESTIC
      case "PRIMARY_FOREIGN" => PRIMARY_FOREIGN
      case "SECONDARY_DOMESTIC" => SECONDARY_DOMESTIC
      case "SECONDARY_FOREIGN" => SECONDARY_FOREIGN
      case "MOTHERS_DOMESTIC" => MOTHERS_DOMESTIC
      case "MOTHERS_FOREIGN" => MOTHERS_FOREIGN
      case "JDQ_GROWTH_DOMESTIC" => JDQ_GROWTH_DOMESTIC
      case "JDQ_STANDARD_DOMESTIC" => JDQ_STANDARD_DOMESTIC
      case "JDQ_STANDARD_FOREIGN" => JDQ_STANDARD_FOREIGN
      case "PRO" => PRO
      case "ETF" => ETF
      case "REIT" => REIT
      case "EQUITY_SECURITIES" => EQUITY_SECURITIES
      case _ => throw new IllegalArgumentException(s"Couldn't convert $str to ${getClass.getSimpleName}")
    }
  }

}