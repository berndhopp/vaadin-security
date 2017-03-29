package org.ilay;

import com.vaadin.ui.Component;

import java.util.Set;

/**
 * @see {@link Authorization#bindComponent(Component)}
 * @see {@link Authorization#bindComponents(Component...)}
 */
class ComponentBind extends Authorization.Bind<Component> {

    ComponentBind(Component[] components) {
        super(components);
    }

    ComponentBind(Component component) {
        super(component);
    }

    @Override
    protected void bindInternal(Set<Object> permissions) {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        for (Component component : super.tSet) {
            authorizationContext.addPermissions(component, permissions);
        }

        Authorization.rebindInternal(super.tSet, authorizationContext);
    }
}
