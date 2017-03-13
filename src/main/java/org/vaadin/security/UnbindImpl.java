package org.vaadin.security;

import com.vaadin.ui.Component;

import org.vaadin.security.api.Binder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

class UnbindImpl implements Binder.Unbind {

    private final Component[] components;
    private AuthorizationEngine authorizationEngine;

    UnbindImpl(AuthorizationEngine authorizationEngine, Component[] components) {
        this.authorizationEngine = authorizationEngine;
        this.components = components;
    }

    @Override
    public Binder from(Object... permissions) {
        checkNotNull(permissions);
        checkArgument(permissions.length > 0);

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
            authorizationEngine.componentsToPermissions.removeAll(component);
        }

        return authorizationEngine;
    }
}
