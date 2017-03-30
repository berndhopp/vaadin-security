package org.ilay;

import com.vaadin.navigator.View;

import java.util.Map;
import java.util.Set;

class ViewObjectsRegistration extends ObjectsReverter<View> {

    ViewObjectsRegistration(ViewRestrict viewRestrict) {
        super(viewRestrict.restrictionMap);
    }

    @Override
    void revertInternal() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        for (Map.Entry<View, Set<Object>> entry : restrictionsMap.entrySet()) {
            final View view = entry.getKey();
            final Set<Object> permissions = entry.getValue();

            authorizationContext.removePermissions(view, permissions);
        }
    }
}
