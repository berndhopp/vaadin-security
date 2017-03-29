package org.ilay;

import com.vaadin.ui.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @see {@link Authorization#unbindComponent(Component)}
 * @see {@link Authorization#unbindComponents(Component...)}
 */
class ComponentUnbind extends Authorization.Unbind<Component> {

    ComponentUnbind(Component[] tArray) {
        super(tArray);
    }

    ComponentUnbind(Component view) {
        super(view);
    }

    protected void unbindInternal(Set<Object> permissions) {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Set<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();

        super.tSet
                .stream()
                .map(componentsToPermissions::get)
                .filter(Objects::nonNull)
                .forEach(componentPermissions -> componentPermissions.removeAll(permissions));

        Authorization.rebindInternal(super.tSet, authorizationContext);
    }


    @Override
    protected void unbindInternalAll() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Set<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();

        for (Component component : super.tSet) {
            componentsToPermissions.remove(component);
            component.setVisible(true);
        }
    }
}
