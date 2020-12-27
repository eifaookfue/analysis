package jp.co.nri.nefs.tool.oms.order.service.proxy

object OrderServiceProxy {

	val proxy = ComponentContainer.getComponent(classOf[IOrderServiceProxy])

	def newOrder(param: NewOrderParam): IOrderServiceResult[OrderParam] = proxy.newOrder(param)
	def newOrder(params: List[NewOrderParam]): IOrderServiceResult[List[OrderParam]] = proxy.newOrder(params)

	implicit def newOrderParam2Entity(param: NewOrderParam): IEntity[ENewOrderProperty] = param.entity
	implicit def newOrderEntity2Param(entity: IEntity[ENewOrderProperty]): NewOrderParam = NewOrderParam(entity)
}
