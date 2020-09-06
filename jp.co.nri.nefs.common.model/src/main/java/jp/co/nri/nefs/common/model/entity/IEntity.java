package jp.co.nri.nefs.common.model.entity;

public interface IEntity<E extends Enum<E>> {

    <T> T getValue(E e);

}
