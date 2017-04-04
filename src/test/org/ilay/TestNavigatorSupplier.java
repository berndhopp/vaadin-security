package org.ilay;

import com.vaadin.navigator.ViewChangeListener;

import java.util.Optional;
import java.util.function.Supplier;

class TestNavigatorSupplier implements Supplier<Optional<VaadinAbstraction.NavigatorFacade>> {

    @Override
    public Optional<VaadinAbstraction.NavigatorFacade> get() {
        return Optional.of(new VaadinAbstraction.NavigatorFacade() {
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
