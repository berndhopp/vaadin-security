package org.ilay;

import com.vaadin.navigator.ViewChangeListener;

interface NavigatorFacade {
    String getState();

    void navigateTo(String s);

    void addViewChangeListener(ViewChangeListener viewChangeListener);
}
