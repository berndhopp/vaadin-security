package org.ilay;

import com.vaadin.navigator.Navigator;
import com.vaadin.ui.UI;

import java.util.function.Supplier;

public class ProductionNavigatorSupplier implements Supplier<Navigator> {
    @Override
    public Navigator get() {
        return UI.getCurrent().getNavigator();
    }
}
