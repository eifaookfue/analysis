package jp.co.nri.nefs.tool.oms.order.service.entity.param

import jp.co.nri.nefs.common.model.entity.DefaultEntity
import jp.co.nri.nefs.common.model.entity.IEntity
import jp.co.nri.nefs.oms.entity.property.definition.EBSType
import jp.co.nri.nefs.oms.entity.property.definition.EMarket
import jp.co.nri.nefs.oms.order.service.entity.property.{ENewOrderProperty => NO}
import jp.co.nri.nefs.tool.ViewCodeWrapper._

case class NewOrderParam(
	BASKET_ID: BigDecimal,
	SYMBOL: String,
	BS_TYPE: EBSType = EBSType.B,
	PRICE: Option[BigDecimal] = None,
	ORDERQTY: BigDecimal,
	MARKET: EMarket = EMarket.TYO_MAIN
) extends AbstractProducer[ENewOrderProperty] {
	val entity: IEntity[NO] = DefaultEntity.valueOf(NO.class)
	entity.putValue(NO.BASKET_ID, BASKET_ID.underlying)
	entity.putValue(NO.INSTRUMENT_ID, SYMBOL.toInst)
	entity.putValue(NO.BS_TYPE, BS_TYPE)
	PRICE.foreach(entity.putValue(NO.PRICE, _.underlying))
	entity.putValue(NO.ORDERQTY, ORDERQTY.underlying)
	entity.putValue(NO.MARKET, MARKET)
}

object NewOrderParam {
	def apply(entity: IEntity[NO]): NewOrderParam = NewOrderParam(
		entity.getValue[java.math.BigDecimal](NO.BASKET_ID),
		entity.getValue[String](NO.INSTRUMENT_ID).toSymbol(entity.getValue[String](NO.MARKET)),
		entity.getValue[EBSType](NO.BS_TYPE),
		entity.getValue[java.math.BigDecimal](NO.PRICE),
		entity.getValue[java.math.BigDecimal](NO.ORDERQTY),
		entity.getValue[EMarket](NO.MARKET)
	)
}
