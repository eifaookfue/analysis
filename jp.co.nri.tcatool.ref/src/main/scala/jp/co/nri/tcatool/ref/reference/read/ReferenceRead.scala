package jp.co.nri.tcatool.ref.reference.read

import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.ref.reference.model.EActiveFlag

import scala.util.Try

object ReferenceRead {

  implicit object activeFlagRead extends Read[EActiveFlag] {
    def reads(s: String): Try[EActiveFlag] = Try(
      s match {
        case "0" => EActiveFlag.INACTIVE
        case "1" => EActiveFlag.ACTIVE
        case _ => throw new IllegalArgumentException(s"Couldn't convert $s to EActiveFlag")
      }
    )
  }

}
