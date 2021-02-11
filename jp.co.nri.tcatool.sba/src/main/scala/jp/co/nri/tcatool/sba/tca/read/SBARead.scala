package jp.co.nri.tcatool.sba.tca.read

import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.ref.reference.model.{EBSType, EMarket, ESessionType}
import jp.co.nri.tcatool.sba.model.EHistoryType

import scala.util.Try

object SBARead {

  implicit object marketRead extends Read[EMarket] {

    import EMarket._

    override def reads(s: String): Try[EMarket] = Try(
      s.trim match {
        case "J000" => TSE
        case "J002" => NGO
        case "CHJ" => CHJ
        case "JNX" => JNX
        case "PTS" => PTS // TODO
        case "DKPL" => DKPL
        case _ => throw new NoSuchElementException(s"Couldn't convert $s to $getClass")
      }
    )
  }

  implicit object sessionTypeRead extends Read[ESessionType] {

    import ESessionType._

    override def reads(s: String): Try[ESessionType] = Try(
      s.trim match {
        case "NULL" => NONE
        case "5"   => AM_ZARABA
        case "6"   => PM_ZARABA
        case "7"   => ALL_DAY
        case _ => throw new NoSuchElementException(s"Couldn't convert $s to $getClass")
      }
    )
  }

  implicit object bsTypeRead extends Read[EBSType] {
    override def reads(s: String): Try[EBSType] = Try(
      EBSType.valueOf(s.trim)
    )
  }

  implicit object historyTypeRead extends Read[EHistoryType] {

    import EHistoryType._

    override def reads(s: String): Try[EHistoryType] = Try(
      s.trim match {
        case "0" => NEW
        case "1" => AMEND
        case "2" => CANCEL
        case _ => throw new NoSuchElementException(s"Couldn't convert $s to $getClass")
      }
    )
  }

}
