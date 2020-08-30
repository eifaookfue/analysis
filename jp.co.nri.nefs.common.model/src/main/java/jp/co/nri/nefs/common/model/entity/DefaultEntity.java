package jp.co.nri.nefs.common.model.entity;

public class DefaultEntity<E extends Enum<E>> implements IEntity<E> {

    public <T> T getValue() {
        return null;
    }

    void putValue(E property) {
    }
}
