package org.ilay;

import com.vaadin.ui.Component;

import java.util.Collection;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public class Bind {

    private final Component[] components;

    Bind(Component[] components) {
        requireNonNull(components);
        if (components.length == 0) {
            throw new IllegalArgumentException("components must not be empty");
        }

        this.components = components;
    }

    public void to(Object... permissions) {
        requireNonNull(permissions);
        if (permissions.length == 0) {
            throw new IllegalArgumentException("one ore more permissions needed");
        }

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Collection<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();

        for (Component component : components) {

            final Collection<Object> currentPermissions = componentsToPermissions.get(component);

            final Collection<Object> newPermissions = asList(permissions);

            if (currentPermissions == null) {
                componentsToPermissions.put(component, newPermissions);
            } else {
                currentPermissions.addAll(newPermissions);
            }
        }

        Authorization.apply(components, authorizationContext);
    }
}
