package org.vaadin.security.impl;

import com.vaadin.navigator.View;
import org.vaadin.security.api.Binder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

class ViewBindImpl implements Binder.Bind {

    private AuthorizationEngine authorizationEngine;
    private final View[] views;

    ViewBindImpl(AuthorizationEngine authorizationEngine, View[] views) {
        this.authorizationEngine = authorizationEngine;
        this.views = views;
    }

    @Override
    public Binder to(Object... permissions) {
        checkNotNull(permissions);
        checkArgument(permissions.length > 0, "one ore more permissions needed");

        for (View view : views) {
            authorizationEngine.viewsToPermissions.putAll(view, asList(permissions));
        }

        return authorizationEngine;
    }
}
