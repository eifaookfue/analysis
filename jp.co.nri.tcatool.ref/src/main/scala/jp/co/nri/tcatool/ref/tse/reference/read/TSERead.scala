package jp.co.nri.tcatool.ref.tse.reference.read

import jp.co.nri.tcatool.common.read.Read
import jp.co.nri.tcatool.ref.reference.model.EProductType

import scala.util.{Failure, Success, Try}

object TSERead {

  implicit object productTypeRead extends Read[EProductType] {

    import EProductType._

    private val productTypes: Map[String, EProductType] = Map(
      "市場第一部（内国株）" -> PRIMARY_DOMESTIC,
      "市場第一部（外国株）" -> PRIMARY_FOREIGN,
      "市場第二部（内国株）" -> SECONDARY_DOMESTIC,
      "市場第二部（外国株）" -> SECONDARY_FOREIGN,
      "マザーズ（内国株）" -> MOTHERS_DOMESTIC,
      "マザーズ（外国株）" -> MOTHERS_FOREIGN,
      "JASDAQ(グロース・内国株）" -> JDQ_GROWTH_DOMESTIC,
      "JASDAQ(スタンダード・外国株）" -> JDQ_STANDARD_DOMESTIC,
      "PRO Market" -> PRO,
      "ETF・ETN" -> ETF,
      "REIT・ベンチャーファンド・カントリーファンド・インフラファンド" -> REIT,
      "出資証券" -> EQUITY_SECURITIES
    )

    override def reads(s: String): Try[EProductType] = {
      productTypes.get(s).map(Success(_)).getOrElse(Failure(new NoSuchElementException(s)))
    }
  }

  implicit object optionalProductTypeRead extends Read[Option[EProductType]] {
    override def reads(s: String): Try[Option[EProductType]] = {
      Read[EProductType].reads(s).map(Some(_)).orElse(Success(None))
    }
  }

}
