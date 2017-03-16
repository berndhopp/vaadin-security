package org.vaadin.security;

import com.vaadin.ui.Component;

import org.vaadin.security.api.Binder;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

class BindImpl implements Binder.Bind {

    private final Component[] components;
    private AuthorizationEngine authorizationEngine;

    BindImpl(AuthorizationEngine authorizationEngine, Component[] components) {
        this.authorizationEngine = authorizationEngine;
        this.components = components;
    }

    @Override
    public Binder to(Object... permissions) {
        requireNonNull(permissions);
        if (permissions.length == 0) {
            throw new IllegalArgumentException("one ore more permissions needed");
        }

        for (Component component : components) {

            final Collection<Object> currentPermissions = authorizationEngine.componentsToPermissions.get(component);

            final Collection<Object> newPermissions = asList(permissions);

            if (currentPermissions == null) {
                authorizationEngine.componentsToPermissions.put(component, newPermissions);
            } else {
                currentPermissions.addAll(newPermissions);
            }
        }

        authorizationEngine.apply(components);

        return authorizationEngine;
    }
}
