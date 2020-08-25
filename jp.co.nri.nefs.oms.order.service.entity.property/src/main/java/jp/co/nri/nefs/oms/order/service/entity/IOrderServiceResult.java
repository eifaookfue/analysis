package jp.co.nri.nefs.oms.order.service.entity;

public interface IOrderServiceResult<T> {

    EStatus getStatus();

    String getErrorReason();

    T getRecord();

    public enum EStatus {
        SUCCESS,
        WARNING,
        ERROR
    }
}
