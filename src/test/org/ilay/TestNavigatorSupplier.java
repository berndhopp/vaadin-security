package org.ilay;

import com.vaadin.navigator.ViewChangeListener;

import java.util.function.Supplier;

class TestNavigatorSupplier implements Supplier<VaadinAbstraction.NavigatorFacade> {

    @Override
    public VaadinAbstraction.NavigatorFacade get() {
        return new VaadinAbstraction.NavigatorFacade() {
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
