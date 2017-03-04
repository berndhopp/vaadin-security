package org.vaadin.security.impl;

import com.vaadin.navigator.View;
import org.vaadin.security.api.Binder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

class ViewUnbindImpl implements Binder.Unbind {

    private final View[] views;
    private AuthorizationEngine authorizationEngine;

    ViewUnbindImpl(AuthorizationEngine authorizationEngine, View[] views) {
        this.authorizationEngine = authorizationEngine;
        this.views = views;
    }

    @Override
    public Binder from(Object... permissions) {
        checkNotNull(permissions);
        checkArgument(permissions.length > 0);

        for (View view : views) {
            for (Object permission : permissions) {
                authorizationEngine.viewsToPermissions.remove(view, permission);
            }
        }

        return authorizationEngine;
    }

    @Override
    public Binder fromAll() {
        for (View view : views) {
            authorizationEngine.componentsToPermissions.removeAll(view);
        }

        return authorizationEngine;
    }
}
