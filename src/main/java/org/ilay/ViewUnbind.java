package org.ilay;

import com.vaadin.navigator.View;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
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
        Check.arg(permissions.length != 0, "permissions cannot be empty");

        Collection<Object> permissionsCollection = asList(permissions);

        final Map<View, Set<Object>> viewsToPermissions = AuthorizationContext
                .getCurrent()
                .getViewsToPermissions();

        stream(views)
                .map(viewsToPermissions::get)
                .filter(p -> p != null)
                .forEach(p -> p.removeAll(permissionsCollection));
    }

    public void fromAll() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<View, Set<Object>> viewsToPermissions = authorizationContext.getViewsToPermissions();

        for (View view : views) {
            viewsToPermissions.remove(view);
        }
    }
}
