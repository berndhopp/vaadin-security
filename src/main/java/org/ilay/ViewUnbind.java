package org.ilay;

import com.vaadin.navigator.View;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ViewUnbind {

    private final View[] views;

    ViewUnbind(View[] views) {
        requireNonNull(views);

        if (views.length == 0) {
            throw new IllegalArgumentException("components must not be empty");
        }

        this.views = views;
    }

    public void from(Object... permissions) {
        requireNonNull(permissions);
        if (permissions.length == 0) {
            throw new IllegalArgumentException("permissions cannot be empty");
        }

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<View, Set<Object>> viewsToPermissions = authorizationContext.getViewsToPermissions();

        for (View view : views) {
            final Collection<Object> existingPermissions = viewsToPermissions.get(view);

            if (existingPermissions != null) {
                for (Object permission : permissions) {
                    existingPermissions.remove(permission);
                }
            }
        }
    }

    public void fromAll() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<View, Set<Object>> viewsToPermissions = authorizationContext.getViewsToPermissions();

        for (View view : views) {
            viewsToPermissions.remove(view);
        }
    }
}