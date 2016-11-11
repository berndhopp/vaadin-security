package org.vaadin.security.impl;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.UI;

import org.vaadin.security.annotation.Restricted;
import org.vaadin.security.api.PermissionEvaluator;
import org.vaadin.security.api.SecureView;

/**
 * Attach to a {@link com.vaadin.navigator.Navigator} to have view-navigation checked for permissions.
 * {@link SecureViewChangeListener} will prevent navigation to {@link View}s that are not accessible by
 * their {@link Restricted} annotation or {@link SecureView#canAccess(String)}.
 * SecureViewChangeListener instances will be created and attached to your navigator automatically
 * by DI-framework integration ( VaadinSpring, GuiceVaadin ).
 */
public abstract class SecureViewChangeListener implements ViewChangeListener {

    private final String permissionDeniedTarget;
    private final PermissionEvaluator permissionEvaluator;

    /**
     * @param permissionDeniedTarget will be navigated to if a view is inaccessible, see {@link com.vaadin.navigator.Navigator#navigateTo(String)}
     * @param permissionEvaluator the {@link PermissionEvaluator} to be used to evaluate {@link Restricted#value()}
     */
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
