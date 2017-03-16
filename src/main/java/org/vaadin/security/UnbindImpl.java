package org.vaadin.security;

import com.vaadin.ui.Component;

import org.vaadin.security.api.Binder;

import static java.util.Objects.requireNonNull;

class UnbindImpl implements Binder.Unbind {

    private final Component[] components;
    private AuthorizationEngine authorizationEngine;

    UnbindImpl(AuthorizationEngine authorizationEngine, Component[] components) {
        this.authorizationEngine = authorizationEngine;
        this.components = components;
    }

    @Override
    public Binder from(Object... permissions) {
        requireNonNull(permissions);
        if (permissions.length == 0) {
            throw new IllegalArgumentException("permissions cannot be empty");
        }

        for (Component component : components) {

            for (Object permission : permissions) {
                authorizationEngine.componentsToPermissions.remove(component, permission);
            }
        }

        return authorizationEngine;
    }

    @Override
    public Binder fromAll() {
        for (Component component : components) {
            authorizationEngine.componentsToPermissions.remove(component);
        }

        return authorizationEngine;
    }
}
