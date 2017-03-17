package org.vaadin.authorization;

import com.vaadin.navigator.View;

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;

class ViewUnbindImpl implements Authorization.Unbind {

    private final View[] views;

    ViewUnbindImpl(View[] views) {
        requireNonNull(views);

        if (views.length == 0) {
            throw new IllegalArgumentException("components must not be empty");
        }

        this.views = views;
    }

    @Override
    public void from(Object... permissions) {
        requireNonNull(permissions);
        if (permissions.length == 0) {
            throw new IllegalArgumentException("permissions cannot be empty");
        }

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<View, Collection<Object>> viewsToPermissions = authorizationContext.getViewsToPermissions();

        for (View view : views) {
            final Collection<Object> existingPermissions = viewsToPermissions.get(view);

            if (existingPermissions != null) {
                for (Object permission : permissions) {
                    existingPermissions.remove(permission);
                }
            }
        }
    }

    @Override
    public void fromAll() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<View, Collection<Object>> viewsToPermissions = authorizationContext.getViewsToPermissions();

        for (View view : views) {
            viewsToPermissions.remove(view);
        }
    }
}
