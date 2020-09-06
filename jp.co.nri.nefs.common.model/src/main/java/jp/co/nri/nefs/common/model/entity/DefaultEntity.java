package jp.co.nri.nefs.common.model.entity;

import java.util.HashMap;
import java.util.Map;

public class DefaultEntity<E extends Enum<E>> implements IEntity<E> {

    private Map<E, Object> map;

    private DefaultEntity() {
        this.map = new HashMap<E, Object>();
    }

    public static <T extends Enum<T>> DefaultEntity<T> valueOf(Class<T> propertyClass) {
        return new DefaultEntity();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(E e) {
        return (T)map.get(e);
    }

    public void putValue(E key, Object value) {
        map.put(key, value);
    }
}
