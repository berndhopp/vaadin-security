package org.ilay;

import com.vaadin.ui.Component;

import java.util.Map;
import java.util.Set;

/**
 * @author Bernd Hopp bernd@vaadin.com
 * @see Authorization#restrictComponent(Component)
 * @see Authorization#restrictComponents(Component...)
 */
class ComponentRestrictRegistration extends RestrictRegistrationImpl<Component> {

    ComponentRestrictRegistration(Component[] components) {
        super(components);
    }

    ComponentRestrictRegistration(Component component) {
        super(component);
    }

    @Override
    protected ObjectBasedPermissionAssignmentRegistration<Component> createRegistration() {
        return new ComponentObjectBasedPermissionAssignmentRegistration(restrictionMap);
    }

    @Override
    protected void bindInternal() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        for (Map.Entry<Component, Set<Object>> componentSetEntry : super.restrictionMap.entrySet()) {

            final Component component = componentSetEntry.getKey();
            final Set<Object> permissions = componentSetEntry.getValue();

            authorizationContext.addPermissions(component, permissions);
        }
    }

}
