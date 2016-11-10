package org.vaadin.security.api;

import com.vaadin.ui.Component;

import org.vaadin.security.annotation.Restricted;

/**
 * PermissionEnforcer will make sure that every permission is applied so that only
 * those {@link Component}s are visible where the needed permission is granted. Permissions
 * in this context come in the form of Strings and are connected to components either on
 * a class-level by {@link Restricted#value()} if some DI-integration is used ( Spring, Guice, CDI ),
 * or on an instance-level by calling {@link PermissionEnforcer#register(Component, String)}.
 * A {@link PermissionEvaluator} is used to evaluate wether or not a permission is granted or not
 * for the current user.
 *
 * @author Bernd Hopp
 */
public interface PermissionEnforcer {

    /**
     * evaluate permissions for all registered components and make only those components visible,
     * that are either not connected to a permission or are connected to a permission which has been granted,
     * see also {@link Component#setVisible(boolean)}
     */
    void enforce();

    /**
     * register a component with a permission, which connects the component to the permission
     * and will be applied when {@link PermissionEnforcer#enforce()} is being called.
     * @param component the {@link Component}
     * @param permission the permission
     */
    void register(Component component, String permission);
}
