package org.ilay;

import com.vaadin.ui.Component;

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ComponentUnbind {

    private final Component[] components;

    ComponentUnbind(Component[] components) {
        requireNonNull(components);

        if (components.length == 0) {
            throw new IllegalArgumentException("components must not be empty");
        }

        this.components = components;
    }

    public void from(Object... permissions) {
        requireNonNull(permissions);
        if (permissions.length == 0) {
            throw new IllegalArgumentException("permissions cannot be empty");
        }

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Collection<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();

        for (Component component : components) {
            final Collection<Object> componentPermissions = componentsToPermissions.get(component);

            for (Object permission : permissions) {
                componentPermissions.remove(permission);
            }
        }
    }

    public void fromAll() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Collection<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();

        for (Component component : components) {
            componentsToPermissions.remove(component);
        }
    }
}
