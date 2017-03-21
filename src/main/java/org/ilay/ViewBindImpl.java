package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.server.VaadinSession;

import java.util.Collection;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

class ViewBindImpl implements Bind {

    private static final String OPEN_BIND_KEY = "open_bind";
    private static final String OPEN_BIND_EXCEPTION_MESSAGE = "cannot bind  when bind is in progress, did you call just \"bind()\"; instead of \"bind().to()\"?";
    private final View[] views;

    ViewBindImpl(View[] views) {
        requireNonNull(views);
        if (views.length == 0) {
            throw new IllegalArgumentException("views must not be empty");
        }

        this.views = views;

        final VaadinSession vaadinSession = VaadinSession.getCurrent();

        if (vaadinSession.getAttribute(OPEN_BIND_KEY) != null) {
            throw new IllegalStateException(OPEN_BIND_EXCEPTION_MESSAGE);
        }

        vaadinSession.setAttribute(OPEN_BIND_KEY, this);
    }

    @Override
    public void to(Object... permissions) {
        requireNonNull(permissions);

        final VaadinSession vaadinSession = VaadinSession.getCurrent();

        if (vaadinSession.getAttribute(OPEN_BIND_KEY) != this) {
            throw new IllegalStateException(OPEN_BIND_EXCEPTION_MESSAGE);
        }

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

        vaadinSession.setAttribute(OPEN_BIND_KEY, null);
    }
}
