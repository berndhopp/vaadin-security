package org.ilay;

import com.vaadin.navigator.ViewChangeListener;

import java.util.function.Supplier;

class TestNavigatorSupplier implements Supplier<TestSupport.NavigatorFacade> {

    @Override
    public TestSupport.NavigatorFacade get() {
        return new TestSupport.NavigatorFacade() {
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
