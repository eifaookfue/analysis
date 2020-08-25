package jp.co.nri.nefs.common.model;

public interface IEntity<E extends Enum<E>> {

    <T> T getValue();

    void putValue(E property);

}
