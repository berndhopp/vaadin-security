package org.ilay;

import com.vaadin.ui.Component;

import java.util.Map;
import java.util.Set;

class ComponentObjectsRegistration extends ObjectsRegistration<Component> {

    ComponentObjectsRegistration(Map<Component, Set<Object>> permissions) {
        super(permissions);
    }

    @Override
    void revertInternal() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        for (Map.Entry<Component, Set<Object>> entry : restrictionsMap.entrySet()) {
            final Component component = entry.getKey();
            final Set<Object> restrictions = entry.getValue();

            authorizationContext.removePermissions(component, restrictions);
        }
    }
}
