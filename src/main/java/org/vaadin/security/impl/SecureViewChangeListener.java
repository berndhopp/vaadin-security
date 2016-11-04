package org.vaadin.security.impl;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.UI;

import org.vaadin.security.annotation.Restricted;
import org.vaadin.security.api.PermissionEvaluator;
import org.vaadin.security.api.SecureView;

public class SecureViewChangeListener implements ViewChangeListener {

    private final String permissionDeniedTarget;
    private final PermissionEvaluator permissionEvaluator;

    public SecureViewChangeListener(String permissionDeniedTarget, PermissionEvaluator permissionEvaluator) {
        if(permissionDeniedTarget == null) throw new IllegalArgumentException("permissionDeniedTarget cannot be null or empty");
        if(permissionEvaluator == null) throw new IllegalArgumentException("permissionEvaluator cannot be null");
        this.permissionDeniedTarget = permissionDeniedTarget;
        this.permissionEvaluator = permissionEvaluator;
    }

    public boolean beforeViewChange(ViewChangeEvent event) {

        final View newView = event.getNewView();

        boolean canAccess = true;

        if (newView instanceof SecureView) {
            canAccess = ((SecureView) newView).canAccess(event.getParameters());
        } else {
            final Class<? extends View> viewClass = newView.getClass();

            final Restricted annotation = viewClass.getAnnotation(Restricted.class);

            if(annotation != null){
                canAccess = permissionEvaluator.hasPermission(annotation.value());
            }
        }

        if (!canAccess) {
            UI.getCurrent().getNavigator().navigateTo(permissionDeniedTarget);
        }

        return canAccess;
    }

    public void afterViewChange(ViewChangeEvent event) {
    }
}
