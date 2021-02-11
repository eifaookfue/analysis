package jp.co.nri.tcatool.ref.alpha.common.read

import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.ref.reference.model.{EMarket, ESessionType}

import scala.util.Try

object AlphaRead {

  implicit object marketRead extends Read[EMarket] {
    def reads(s: String): Try[EMarket] = Try(marketCreator(s))
  }

  private def marketCreator(code: String): EMarket = code match {
    case "1" => EMarket.TSE
    case "2" => EMarket.OSA
    case "3" => EMarket.NGO
    case "4" => EMarket.KYO
    case "6" => EMarket.FKA
    case "8" => EMarket.SAP
    case "J" => EMarket.JDQ
    case "N" => EMarket.NDQ
    case _ => throw new IllegalArgumentException(s"Couldn't convert $code to EMarket")
  }

  implicit object sessionRead extends Read[ESessionType] {
    def reads(s: String): Try[ESessionType] = Try(sessionCreator(s))
  }

  private def sessionCreator(code: String): ESessionType = code match {
    case "1" => ESessionType.AM_PRE_OPEN
    case "2" => ESessionType.AM_OPEN
    case "3" => ESessionType.AM_ZARABA
    case "5" => ESessionType.PM_PRE_OPEN
    case "6" => ESessionType.PM_OPEN
    case "7" => ESessionType.PM_ZARABA
    case _ => throw new IllegalArgumentException(s"Couldn't convert $code to ESessionType")
  }

}
