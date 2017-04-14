package org.ilay;

import com.vaadin.ui.Component;

import java.util.Map;
import java.util.Set;

/**
 * @see Authorization#restrictComponent(Component)
 * @see Authorization#restrictComponents(Component...)
 */
class ComponentRestrict extends RestrictImpl<Component> {

    ComponentRestrict(Component[] components) {
        super(components);
    }

    ComponentRestrict(Component component) {
        super(component);
    }

    @Override
    protected ObjectBasedPermissionAssignmentReverter<Component> createReverter() {
        return new ComponentObjectBasedPermissionAssignmentReverter(restrictionMap);
    }

    @Override
    protected void bindInternal() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        for (Map.Entry<Component, Set<Object>> componentSetEntry : super.restrictionMap.entrySet()) {

            final Component component = componentSetEntry.getKey();
            final Set<Object> permissions = componentSetEntry.getValue();

            authorizationContext.addPermissions(component, permissions);
        }

        Authorization.reapplyInternal(super.restrictionMap, authorizationContext);
    }

}
