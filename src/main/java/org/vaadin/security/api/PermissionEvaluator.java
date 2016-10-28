package org.vaadin.security.api;

public interface PermissionEvaluator {
    boolean hasPermission(String permission);
}
