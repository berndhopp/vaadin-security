package org.ilay;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.UI;

import java.util.Objects;
import java.util.function.Supplier;

class ProductionNavigatorFacadeSupplier implements Supplier<NavigatorFacade> {
    @Override
    public NavigatorFacade get() {
        final UI ui = Objects.requireNonNull(UI.getCurrent());

        Navigator navigator = ui.getNavigator();

        if(navigator == null){
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
