package org.ilay;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * a couple of abstractions to support testing
 *
 * @author Bernd Hopp
 */
class VaadinAbstraction {

    private static Supplier<Optional<NavigatorFacade>> navigatorSupplier = new ProductionNavigatorFacadeSupplier();
    private static Supplier<SessionInitNotifier> sessionInitNotifierSupplier = new ProductionSessionInitNotifierSupplier();
    private static SessionStore sessionStore = new SessionStore() {
        @Override
        public <T> Optional<T> get(Class<T> tClass) {
            return Optional.ofNullable(VaadinSession.getCurrent().getAttribute(tClass));
        }

        @Override
        public <T> void set(Class<T> tClass, T t) {
            VaadinSession.getCurrent().setAttribute(tClass, t);
        }
    };

    static void setNavigatorSupplier(Supplier<Optional<NavigatorFacade>> navigatorSupplier) {
        requireNonNull(navigatorSupplier);
        VaadinAbstraction.navigatorSupplier = navigatorSupplier;
    }

    static void setSessionInitNotifierSupplier(Supplier<SessionInitNotifier> sessionInitNotifierSupplier) {
        requireNonNull(sessionInitNotifierSupplier);
        VaadinAbstraction.sessionInitNotifierSupplier = sessionInitNotifierSupplier;
    }

    static Optional<NavigatorFacade> getNavigatorFacade() {
        return navigatorSupplier.get();
    }

    static SessionInitNotifier getSessionInitNotifier() {
        return sessionInitNotifierSupplier.get();
    }

    static void setSessionStore(SessionStore sessionStore) {
        VaadinAbstraction.sessionStore = sessionStore;
    }

    static <T> void storeInSession(Class<T> tClass, T t) {
        sessionStore.set(tClass, t);
    }

    static <T> Optional<T> getFromSessionStore(Class<T> tClass) {
        return sessionStore.get(tClass);
    }

    interface SessionStore {
        <T> Optional<T> get(Class<T> tClass);

        <T> void set(Class<T> tClass, T t);
    }

    interface NavigatorFacade {
        String getState();

        void navigateTo(String s);

        void addViewChangeListener(ViewChangeListener viewChangeListener);
    }

    interface SessionInitNotifier {
        void addSessionInitListener(SessionInitListener listener);
    }

    static class ProductionNavigatorFacadeSupplier implements Supplier<Optional<NavigatorFacade>> {
        @Override
        public Optional<NavigatorFacade> get() {
            final UI ui = requireNonNull(UI.getCurrent());

            Navigator navigator = ui.getNavigator();

            if (navigator == null) {
                return Optional.empty();
            }

            return Optional.of(new NavigatorFacade() {
                @Override
                public String getState() {
                    return navigator.getState();
                }

                @Override
                public void navigateTo(String s) {
                    navigator.navigateTo(s);
                }

                @Override
                public void addViewChangeListener(ViewChangeListener viewChangeListener) {
                    navigator.addViewChangeListener(viewChangeListener);
                }
            });
        }
    }

    static class ProductionSessionInitNotifierSupplier implements Supplier<SessionInitNotifier> {
        @Override
        public SessionInitNotifier get() {
            final VaadinService vaadinService = VaadinService.getCurrent();

            if (vaadinService == null) {
                throw new IllegalStateException("VaadinService is not initialized yet");
            }

            return vaadinService::addSessionInitListener;
        }
    }
}
