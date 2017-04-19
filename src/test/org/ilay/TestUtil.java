package org.ilay;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

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

    public static class TestSessionInitNotifierSupplier implements VaadinAbstraction.SessionInitNotifier, Supplier<VaadinAbstraction.SessionInitNotifier> {

        private List<SessionInitListener> sessionInitListeners = new ArrayList<>();

        @Override
        public VaadinAbstraction.SessionInitNotifier get() {
            return this;
        }

        @Override
        public void addSessionInitListener(SessionInitListener listener) {
            sessionInitListeners.add(listener);
        }

        public void newSession() throws ServiceException {
            for (SessionInitListener sessionInitListener : sessionInitListeners) {
                sessionInitListener.sessionInit(null);
            }
        }
    }

    static class TestNavigatorSupplier implements Supplier<Optional<VaadinAbstraction.Navigator>> {

        @Override
        public Optional<VaadinAbstraction.Navigator> get() {
            return Optional.of(new VaadinAbstraction.Navigator() {
                @Override
                public String getState() {
                    return null;
                }

                @Override
                public void navigateTo(String s) {
                }

                @Override
                public void addViewChangeListener(ViewChangeListener viewChangeListener) {
                }
            });
        }
    }
}
