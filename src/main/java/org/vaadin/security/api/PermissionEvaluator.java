package org.vaadin.security.api;

/**
 * PermissionEvaluator is the object responsible of deciding of a certain permission, which comes in the form of a String,
 * is granted in the current context or not. Usually the "current context" is the currently logged in user and it's roles.
 *
 * @author Bernd Hopp
 */
public interface PermissionEvaluator {

    /**
     * evaluate if a certain permission is granted in the current context
     * @param permission the permission
     * @return true if the permission is granted, otherwise false
     */
    boolean hasPermission(String permission);
}
