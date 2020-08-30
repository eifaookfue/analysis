package jp.co.nri.nefs.tool.oms.order.service.proxy

import jp.co.nri.nefs.common.di.ComponentContainer
import jp.co.nri.nefs.oms.order.service.entity.IOrderServiceResult
import jp.co.nri.nefs.oms.order.service.entity.property.ENewOrderProperty
import jp.co.nri.nefs.oms.order.service.proxy.IOrderServiceProxy
import jp.co.nri.nefs.tool.oms.order.service.entity.param.NewOrderParam

object OrderServiceProxyTarget {

  val proxy = ComponentContainer.getComponent(classOf[IOrderServiceProxy])

  //def newOrder(param: NewOrderParam): IOrderServiceResult[IEntity[EOrderProperty]]
  def newOrder(param: NewOrderParam): IOrderServiceResult[OrderParam] = proxy.newOrder(param)
  def newOrder(params: List[NewOrderParam]): IOrderServiceResult[List[OrderParam]] = proxy.newOrder(params)

  implicit def newOrderParam2Entity(param: NewOrderParam): IEntity[ENewOrderProperty] = param.entity
  implicit def newOrderEntity2Param(entity: IEntity[ENewOrderProperty]): NewOrderParam = NewOrderParam(entity)

}
