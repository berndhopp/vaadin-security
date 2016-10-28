package org.vaadin.security.api;

import com.vaadin.ui.Component;

public interface PermissionEvaluator {
    boolean hasPermission(String permission);
}
