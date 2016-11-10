package org.vaadin.security.api;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;

/**
 * Implement this interface if fine-grained control over the access to this view is needed.
 * A {@link SecureView} has to decide before every access whether it can be accessed with the given
 * parameters in the current context or not. If access is not granted, the user will be navigated to
 * {@link org.vaadin.security.impl.SecureViewChangeListener#permissionDeniedTarget} or the default page
 * if no permissionDeniedTarget is configured.
 *
 * A {@link org.vaadin.security.impl.SecureViewChangeListener} needs to be attached to the {@link com.vaadin.navigator.Navigator}
 * in order for {@link SecureView} to be applied for the navigation
 */
public interface SecureView extends View {

    /**
     * decides whether access is granted for the given parameters in the current context
     * @param parameters see {@link ViewChangeListener.ViewChangeEvent#getParameters()}
     * @return true if access is granted, otherwise false
     */
    boolean canAccess(String parameters);
}
