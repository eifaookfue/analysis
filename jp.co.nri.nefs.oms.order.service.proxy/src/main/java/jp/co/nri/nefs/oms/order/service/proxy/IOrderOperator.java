package jp.co.nri.nefs.oms.order.service.proxy;

import jp.co.nri.nefs.common.model.entity.IEntity;
import jp.co.nri.nefs.oms.entity.property.EOrderProperty;
import jp.co.nri.nefs.oms.order.service.entity.IOrderServiceResult;
import jp.co.nri.nefs.oms.order.service.entity.property.ENewOrderProperty;

import java.util.List;

public interface IOrderOperator {

    IOrderServiceResult<IEntity<EOrderProperty>> newOrder(IEntity<ENewOrderProperty> order);

    IOrderServiceResult<List<IEntity<EOrderProperty>>> newOrder(List<IEntity<ENewOrderProperty>> orders);
}
