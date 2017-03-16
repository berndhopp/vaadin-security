package org.vaadin.security;

import com.vaadin.navigator.View;

import org.vaadin.security.api.Binder;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

class ViewBindImpl implements Binder.Bind {

    private final View[] views;
    private AuthorizationEngine authorizationEngine;

    ViewBindImpl(AuthorizationEngine authorizationEngine, View[] views) {
        this.authorizationEngine = authorizationEngine;
        this.views = views;
    }

    @Override
    public Binder to(Object... permissions) {
        requireNonNull(permissions);
        if (permissions.length == 0) {
            throw new IllegalArgumentException("one ore more permissions needed");
        }

        for (View view : views) {
            final Collection<Object> currentPermissions = authorizationEngine.viewsToPermissions.get(view);

            final Collection<Object> newPermissions = asList(permissions);

            if (currentPermissions == null) {
                authorizationEngine.viewsToPermissions.put(view, newPermissions);
            } else {
                currentPermissions.addAll(newPermissions);
            }
        }

        return authorizationEngine;
    }
}
