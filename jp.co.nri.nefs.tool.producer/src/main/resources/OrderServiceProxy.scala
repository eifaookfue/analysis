package jp.co.nri.nefs.tool.oms.order.service.proxy

import jp.co.nri.nefs.common.di.ComponentContainer
import jp.co.nri.nefs.oms.order.service.entity.IOrderServiceResult
import jp.co.nri.nefs.oms.order.service.entity.property.ENewOrderProperty
import jp.co.nri.nefs.oms.order.service.proxy.IOrderServiceProxy
import jp.co.nri.nefs.tool.oms.order.service.entity.param.NewOrderParam

object OrderServiceProxy {

  val proxy = ComponentContainer.getComponent(classOf[IOrderServiceProxy])

  implicit def newOrderParam2Entity(param: NewOrderParam): IEntity[ENewOrderProperty] = param.entity
  implicit def newOrderEntity2Param(entity: IEntity[ENewOrderProperty]): NewOrderParam = NewOrderParam(entity)

  //def newOrder(param: NewOrderParam): IOrderServiceResult[IEntity[EOrderProperty]]
  def newOrder(param: NewOrderParam): IOrderServiceResult[OrderParam] = proxy.newOrder(param)
  def newOrder(params: Seq[NewOrderParam]): IOrderServiceResult[]
}
