package org.vaadin.security.api;

import com.vaadin.navigator.View;

public interface SecureView extends View {
    boolean canAccess(String parameters);
}
