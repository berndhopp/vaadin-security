package org.ilay;

import com.vaadin.navigator.ViewChangeListener;

import java.util.function.Supplier;

class TestNavigatorSupplier implements Supplier<NavigatorFacade>{

    @Override
    public NavigatorFacade get() {
        return new NavigatorFacade() {
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
        };
    }
}
