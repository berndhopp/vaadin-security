package org.vaadin.security.api;

import com.vaadin.ui.Component;

public interface PermissionEnforcer {
    void enforce();
    void register(Component component, String permission);
}
