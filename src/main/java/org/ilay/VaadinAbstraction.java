package org.ilay;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * a couple of abstractions to support testing
 *
 * @author Bernd Hopp
 */
class VaadinAbstraction {
    interface NavigatorFacade {
        String getState();

        void navigateTo(String s);

        void addViewChangeListener(ViewChangeListener viewChangeListener);
    }

    interface SessionInitNotifier {
        void addSessionInitListener(SessionInitListener listener);
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
