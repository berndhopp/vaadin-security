package org.ilay;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class TestUtil {
    static void beforeTest() throws NoSuchFieldException, IllegalAccessException {
        VaadinAbstraction.setNavigatorSupplier(new TestNavigatorSupplier());
        VaadinAbstraction.setSessionInitNotifierSupplier(new TestSessionInitNotifierSupplier());

        VaadinAbstraction.setSessionStore(new VaadinAbstraction.SessionStore() {

            final Map<Class<?>, Object> map = new HashMap<>();

            @Override
            @SuppressWarnings("unchecked")
            public <T> Optional<T> get(Class<T> tClass) {
                Objects.requireNonNull(tClass);

                return Optional.ofNullable((T) map.get(tClass));
            }

            @Override
            public <T> void set(Class<T> tClass, T t) {
                Objects.requireNonNull(tClass);
                map.put(tClass, t);
            }
        });

        final Field initialized = Authorization.class.getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.set(null, false);
    }
}
