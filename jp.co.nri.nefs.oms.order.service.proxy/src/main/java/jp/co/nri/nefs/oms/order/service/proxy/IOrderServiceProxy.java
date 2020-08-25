package jp.co.nri.nefs.oms.order.service.proxy;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultOrderServiceProxy.class)
public interface IOrderServiceProxy extends IOrderOperator{

}
