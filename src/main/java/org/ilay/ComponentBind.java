package org.ilay;

import com.vaadin.ui.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public class ComponentBind {

    private final Component[] components;

    ComponentBind(Component[] components) {
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
        final Map<Component, Set<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();

        for (Component component : components) {

            Collection<Object> currentPermissions = componentsToPermissions.get(component);

            final Set<Object> newPermissions = new HashSet<>(asList(permissions));

            if (currentPermissions == null) {
                componentsToPermissions.put(component, newPermissions);
            } else {
                currentPermissions.addAll(newPermissions);
            }
        }

        Authorization.apply(components, authorizationContext);
    }
}
