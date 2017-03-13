package org.vaadin.security;

import com.vaadin.ui.Component;

import org.vaadin.security.api.Binder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

class BindImpl implements Binder.Bind {

    private final Component[] components;
    private AuthorizationEngine authorizationEngine;

    BindImpl(AuthorizationEngine authorizationEngine, Component[] components) {
        this.authorizationEngine = authorizationEngine;
        this.components = components;
    }

    @Override
    public Binder to(Object... permissions) {
        checkNotNull(permissions);
        checkArgument(permissions.length > 0, "one ore more permissions needed");

        for (Component component : components) {
            authorizationEngine.componentsToPermissions.putAll(component, asList(permissions));
        }

        authorizationEngine.apply(components);

        return authorizationEngine;
    }
}
