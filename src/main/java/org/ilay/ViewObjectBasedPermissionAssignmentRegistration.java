package org.ilay;

import com.vaadin.navigator.View;

import java.util.Map;
import java.util.Set;

/**
 * @author Bernd Hopp bernd@vaadin.com
 */
class ViewObjectBasedPermissionAssignmentRegistration extends ObjectBasedPermissionAssignmentRegistration<View> {

    private static final long serialVersionUID = -8311040539728937553L;

    ViewObjectBasedPermissionAssignmentRegistration(ViewRestrictRegistration viewRestrict) {
        super(viewRestrict.restrictionMap);
    }

    @Override
    void revertInternal() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        for (Map.Entry<View, Set<Object>> entry : getRestrictionsMap().entrySet()) {
            final View view = entry.getKey();
            final Set<Object> permissions = entry.getValue();

            authorizationContext.removePermissions(view, permissions);
        }
    }
}
