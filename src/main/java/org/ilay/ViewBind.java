package org.ilay;

import com.vaadin.navigator.View;

import java.util.Collection;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

class ViewBind {
    private final View[] views;

    ViewBind(View[] views) {
        requireNonNull(views);
        if (views.length == 0) {
            throw new IllegalArgumentException("views must not be empty");
        }

        this.views = views;
    }

    public void to(Object... permissions) {
        requireNonNull(permissions);

        if (permissions.length == 0) {
            throw new IllegalArgumentException("one ore more permissions needed");
        }

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        final Map<View, Collection<Object>> viewsToPermissions = authorizationContext.getViewsToPermissions();

        for (View view : views) {
            final Collection<Object> currentPermissions = viewsToPermissions.get(view);

            final Collection<Object> newPermissions = asList(permissions);

            if (currentPermissions == null) {
                viewsToPermissions.put(view, newPermissions);
            } else {
                currentPermissions.addAll(newPermissions);
            }
        }
    }
}
