package jp.co.nri.nefs.tool.entity.producer

import java.math.BigDecimal
import jp.co.nri.nefs.oms.entity.property.definition.EBSType
import jp.co.nri.nefs.oms.order.service.entity.property.{ENewOrderProperty => NO}

case class NewOrderParamTarget(
													BASKET_ID: Int,
														SYMBOL: String,
BS_TYPE: EBSType = EBSType.B,
PRICE: Option[BigDecimal] = None,
													MARKET: EMarket = EMarket.TYO_MAIN,
ORDERQTY: Int
) extends AbstractProducer[ENewOrderProperty] {
	val entity = DefaultEntity.valueOf(NO.class)
	entity.putValue(NO.BASKET_ID, java.math.BigDecimal.valueOf[BASKET_ID])
	entity.putValue(ENewOrderProperty.INSTRUMENT_ID, instrumentUtil.getInstrument(SYMBOL, MARKET))
	entity.putValue(ENewOrderProperty.BS_TYPE, BS_TYPE)
	PRICE.foreach(entity.putValue(ENewOrderProperty.PRICE, _.underlying)
	entity.putValue(ENewOrderProperty.ORDERQTY, java.math.BigDecimal.valueOf[ORDERQTY])
}

object NewOrderParamTarget {
	def apply(entity: IEntity[ENewOrderProperty]): NewOrderParamTarget = {
		NewOrderParamTarget(
			entity.getValue(ENewOrderProperty.BASKET_ID),
			instrumentApi.get(InstrumentKey.valueOf(ENewOrderProperty.INSTRUMENT_ID, ENewOrderProperty.MARKET)).getValue(EMarketInstrumentProperty.QUICK_CODE),

		)
	}
}
