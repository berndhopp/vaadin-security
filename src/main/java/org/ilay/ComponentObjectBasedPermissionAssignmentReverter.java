package org.ilay;

import com.vaadin.ui.Component;

import java.util.Map;
import java.util.Set;

class ComponentObjectBasedPermissionAssignmentReverter extends ObjectBasedPermissionAssignmentReverter<Component> {

    ComponentObjectBasedPermissionAssignmentReverter(Map<Component, Set<Object>> permissions) {
        super(permissions);
    }

    @Override
    void revertInternal() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        for (Map.Entry<Component, Set<Object>> entry : getRestrictionsMap().entrySet()) {
            final Component component = entry.getKey();
            final Set<Object> restrictions = entry.getValue();

            authorizationContext.removePermissions(component, restrictions);
        }
    }
}
