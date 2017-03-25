package org.ilay;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class TestSupport {
    interface NavigatorFacade {
        String getState();

        void navigateTo(String s);

        void addViewChangeListener(ViewChangeListener viewChangeListener);
    }

    interface SessionInitNotifier {
        void addSessionInitListener(SessionInitListener listener);
    }

    /**
     * a supplier with an additional set method
     */
    interface Vessel<T> extends Supplier<T> {
        void set(T t);
    }

    static class ProductionAuthorizationContextVessel implements Vessel<AuthorizationContext> {
        @Override
        public AuthorizationContext get() {
            final VaadinSession vaadinSession = VaadinSession.getCurrent();

            requireNonNull(vaadinSession, "no VaadinSession available");

            final AuthorizationContext authorizationContext = vaadinSession.getAttribute(AuthorizationContext.class);

            return requireNonNull(
                    authorizationContext,
                    "no authorizationContext available in the current session, did you forget" +
                            "to call Authorization.start()?"
            );
        }

        @Override
        public void set(AuthorizationContext authorizationContext) {
            final VaadinSession vaadinSession = VaadinSession.getCurrent();

            requireNonNull(vaadinSession, "no VaadinSession available");

            Check.state(vaadinSession.getAttribute(AuthorizationContext.class) == null);

            vaadinSession.setAttribute(AuthorizationContext.class, authorizationContext);
        }
    }

    static class ProductionNavigatorFacadeSupplier implements Supplier<NavigatorFacade> {
        @Override
        public NavigatorFacade get() {
            final UI ui = requireNonNull(UI.getCurrent());

            Navigator navigator = ui.getNavigator();

            if (navigator == null) {
                return null;
            }

            return new NavigatorFacade() {
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
            };
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
