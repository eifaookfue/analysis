package jp.co.nri.nefs.common.di;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ComponentContainer {

    private static Injector injector = Guice.createInjector();

    public static <T> T getComponent(Class<T> clazz) {
        return injector.getInstance(clazz);
    }
}
