package jp.co.nri.nefs.oms.order.service.proxy;

import jp.co.nri.nefs.common.model.entity.IEntity;
import jp.co.nri.nefs.oms.entity.property.EOrderProperty;
import jp.co.nri.nefs.oms.order.service.entity.IOrderServiceResult;
import jp.co.nri.nefs.oms.order.service.entity.property.ENewOrderProperty;

import java.util.List;

public class DefaultOrderServiceProxy implements IOrderServiceProxy{

    public IOrderServiceResult<IEntity<EOrderProperty>> newOrder(IEntity<ENewOrderProperty> order) {
        return null;
    }

    public IOrderServiceResult<List<IEntity<EOrderProperty>>> newOrder(List<IEntity<ENewOrderProperty>> orders) {
        return null;
    }
}
