package jp.co.nri.tcatool.ref.alpha.reference.model

import java.sql.Timestamp
import java.time.LocalDate

import jp.co.nri.tcatool.ref.alpha.common.model.AvgQuoteVolume
import jp.co.nri.tcatool.ref.reference.model.{EMarket, Mapper}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class MI(
             alpha: Option[BigDecimal],
             alpha2: Option[BigDecimal],
             complementFlag: Option[String],
             delta: Option[BigDecimal],
             beta: Option[BigDecimal],
             temp: Option[BigDecimal],
             avgVolumeRatio: Option[BigDecimal]
             )

case class MIZaraba(
                   mi: MI,
                   spread: Option[BigDecimal]
                   )

case class MarketImpact(
                       modelType: String,
                       baseDate: LocalDate,
                       symbol: String,
                       market: EMarket,
                       fromDate: LocalDate,
                       toDate: LocalDate,
                       regFromDate: Option[LocalDate],
                       regToDate: Option[LocalDate],
                       amOpenMI: MI,
                       amZaraba: MIZaraba,
                       pmOpenMI: MI,
                       pmZaraba: MIZaraba,
                       avgVolume: Option[BigDecimal],
                       volatility: Option[BigDecimal],
                       avgTickCount: Option[BigDecimal],
                       avgQuoteVolume: AvgQuoteVolume,
                       idCo: Option[BigDecimal],
                       tValue2: Option[BigDecimal],
                       tValue2Quote: Option[BigDecimal],
                       updateTime: Timestamp
                       )



trait MarketImpactComponent extends Mapper {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class MarketImpacts(tag: Tag) extends Table[MarketImpact](tag, "ALPHA_MARKET_IMPACT") {
    def modelType = column[String]("MODEL_TYPE", O.Length(10))
    def baseDate = column[LocalDate]("BASE_DATE")
    def symbol = column[String]("SYMBOL", O.Length(10))
    def market = column[EMarket]("MARKET", O.Length(10))
    def fromDate = column[LocalDate]("FROM_DATE")
    def toDate = column[LocalDate]("TO_DATE")
    def regFromDate = column[Option[LocalDate]]("REG_FROM_DATE")
    def regToDate = column[Option[LocalDate]]("REG_TO_DATE")
    def amOpenAlpha = column[Option[BigDecimal]]("AM_OPEN_ALPHA")
    def amOpenAlpha2 = column[Option[BigDecimal]]("AM_OPEN_ALPHA2")
    def amOpenComplementFlag = column[Option[String]]("AM_OPEN_COMPLEMENT_FLAG", O.Length(1))
    def amOpenDelta = column[Option[BigDecimal]]("AM_OPEN_DELTA")
    def amOpenBeta = column[Option[BigDecimal]]("AM_OPEN_BETA")
    def amOpenTemp = column[Option[BigDecimal]]("AM_OPEN_TEMP")
    def amOpenAvgVolumeRatio = column[Option[BigDecimal]]("AM_OPEN_AVG_VOLUME_RATIO")
    def amZarabaAlpha = column[Option[BigDecimal]]("AM_ZARABA_ALPHA")
    def amZarabaAlpha2 = column[Option[BigDecimal]]("AM_ZARABA_ALPHA2")
    def amZarabaComplementFlag = column[Option[String]]("AM_ZARABA_COMPLEMENT_FLAG", O.Length(1))
    def amZarabaDelta = column[Option[BigDecimal]]("AM_ZARABA_DELTA")
    def amZarabaBeta = column[Option[BigDecimal]]("AM_ZARABA_BETA")
    def amZarabaTemp = column[Option[BigDecimal]]("AM_ZARABA_TEMP")
    def amZarabaAvgVolumeRatio = column[Option[BigDecimal]]("AM_ZARABA_AVG_VOLUME_RATIO")
    def amZarabaSpread = column[Option[BigDecimal]]("AM_ZARABA_SPREAD")
    def pmOpenAlpha = column[Option[BigDecimal]]("PM_OPEN_ALPHA")
    def pmOpenAlpha2 = column[Option[BigDecimal]]("PM_OPEN_ALPHA2")
    def pmOpenComplementFlag = column[Option[String]]("PM_OPEN_COMPLEMENT_FLAG", O.Length(1))
    def pmOpenDelta = column[Option[BigDecimal]]("PM_OPEN_DELTA")
    def pmOpenBeta = column[Option[BigDecimal]]("PM_OPEN_BETA")
    def pmOpenTemp = column[Option[BigDecimal]]("PM_OPEN_TEMP")
    def pmOpenAvgVolumeRatio = column[Option[BigDecimal]]("PM_OPEN_AVG_VOLUME_RATIO")
    def pmZarabaAlpha = column[Option[BigDecimal]]("PM_ZARABA_ALPHA")
    def pmZarabaAlpha2 = column[Option[BigDecimal]]("PM_ZARABA_ALPHA2")
    def pmZarabaComplementFlag = column[Option[String]]("PM_ZARABA_COMPLEMENT_FLAG", O.Length(1))
    def pmZarabaDelta = column[Option[BigDecimal]]("PM_ZARABA_DELTA")
    def pmZarabaBeta = column[Option[BigDecimal]]("PM_ZARABA_BETA")
    def pmZarabaTemp = column[Option[BigDecimal]]("PM_ZARABA_TEMP")
    def pmZarabaAvgVolumeRatio = column[Option[BigDecimal]]("PM_ZARABA_AVG_VOLUME_RATIO")
    def pmZarabaSpread = column[Option[BigDecimal]]("PM_ZARABA_SPREAD")
    def avgVolume = column[Option[BigDecimal]]("AVG_VOLUME")
    def volatility = column[Option[BigDecimal]]("VOLATILITY", O.SqlType("NUMBER(10,7)"))
    def avgTickCount = column[Option[BigDecimal]]("AVG_TICK_COUNT")
    def askAvgQuoteVolume = column[Option[BigDecimal]]("ASK_AVG_QUOTE_VOLUME")
    def bidAvgQuoteVolume = column[Option[BigDecimal]]("BID_AVG_QUOTE_VOLUME")
    def idCo = column[Option[BigDecimal]]("ID_CO")
    def tValue2 = column[Option[BigDecimal]]("T_VALUE2")
    def tValue2Quote = column[Option[BigDecimal]]("T_VALUE2_QUOTE")
    def updateTime = column[Timestamp]("UPDATE_TIME")
    def pk = primaryKey("ALPHA_MARKET_IMPACT_PK", (modelType, baseDate, symbol, market))

    def * = (modelType, baseDate, symbol, market, fromDate, toDate, regFromDate, regToDate,
      amOpenMIProjection, amZarabaMIProjection, pmOpenMIProjection, pmZarabaMIProjection,
      avgVolume, volatility, avgTickCount, avgQuoteVolumeProjection,
      idCo, tValue2, tValue2Quote, updateTime) <> (MarketImpact.tupled, MarketImpact.unapply)

    def amOpenMIProjection = (amOpenAlpha, amOpenAlpha2, amOpenComplementFlag, amOpenDelta,
      amOpenBeta, amOpenTemp, amOpenAvgVolumeRatio) <> (MI.tupled, MI.unapply)
    def amZarabaProjection = (amZarabaAlpha, amZarabaAlpha2, amZarabaComplementFlag, amZarabaDelta,
      amZarabaBeta, amZarabaTemp, amZarabaAvgVolumeRatio) <> (MI.tupled, MI.unapply)
    def amZarabaMIProjection = (amZarabaProjection, amZarabaSpread) <> (MIZaraba.tupled, MIZaraba.unapply)
    def pmOpenMIProjection = (pmOpenAlpha, pmOpenAlpha2, pmOpenComplementFlag, pmOpenDelta,
      pmOpenBeta, pmOpenTemp, pmOpenAvgVolumeRatio) <> (MI.tupled, MI.unapply)
    def pmZarabaProjection = (pmZarabaAlpha, pmZarabaAlpha2, pmZarabaComplementFlag, pmZarabaDelta,
      pmZarabaBeta, pmZarabaTemp, pmZarabaAvgVolumeRatio) <> (MI.tupled, MI.unapply)
    def pmZarabaMIProjection = (pmZarabaProjection, pmZarabaSpread) <> (MIZaraba.tupled, MIZaraba.unapply)
    def avgQuoteVolumeProjection = (askAvgQuoteVolume, bidAvgQuoteVolume) <>
      (AvgQuoteVolume.tupled, AvgQuoteVolume.unapply)
  }

}
