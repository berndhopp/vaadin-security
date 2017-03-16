package org.vaadin.security;

import com.vaadin.navigator.View;

import org.vaadin.security.api.Binder;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

class ViewUnbindImpl implements Binder.Unbind {

    private final View[] views;
    private AuthorizationEngine authorizationEngine;

    ViewUnbindImpl(AuthorizationEngine authorizationEngine, View[] views) {
        this.authorizationEngine = authorizationEngine;
        this.views = views;
    }

    @Override
    public Binder from(Object... permissions) {
        requireNonNull(permissions);
        if (permissions.length == 0) {
            throw new IllegalArgumentException("permissions cannot be empty");
        }

        for (View view : views) {
            final Collection<Object> existingPermissions = authorizationEngine.viewsToPermissions.get(view);

            if (existingPermissions != null) {
                for (Object permission : permissions) {
                    existingPermissions.remove(permission);
                }
            }
        }

        return authorizationEngine;
    }

    @Override
    public Binder fromAll() {
        for (View view : views) {
            authorizationEngine.viewsToPermissions.remove(view);
        }

        return authorizationEngine;
    }
}
