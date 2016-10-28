package org.vaadin.security.impl;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.UI;

import org.vaadin.security.api.SecureView;

public class SecureViewChangeListener implements ViewChangeListener {

    private final String permissionDeniedTarget;

    public SecureViewChangeListener(String permissionDeniedTarget) {
        if(permissionDeniedTarget == null) throw new IllegalArgumentException("permissionDeniedTarget cannot be null or empty");
        this.permissionDeniedTarget = permissionDeniedTarget;
    }

    public SecureViewChangeListener(){
        this.permissionDeniedTarget = "";
    }

    public boolean beforeViewChange(ViewChangeEvent event) {

        if (!(event.getNewView() instanceof SecureView)) {
            return true;
        }

        boolean canAccess = ((SecureView) event.getNewView()).canAccess(event.getParameters());

        if (!canAccess) {
            UI.getCurrent().getNavigator().navigateTo(permissionDeniedTarget);
        }

        return canAccess;
    }

    public void afterViewChange(ViewChangeEvent event) {
    }
}
