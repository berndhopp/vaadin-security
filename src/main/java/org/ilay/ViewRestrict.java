package org.ilay;

import com.vaadin.navigator.View;

import java.util.Map;
import java.util.Set;

/**
 * @author Bernd Hopp bernd@vaadin.com
 * @see Authorization#restrictView(View)
 * @see Authorization#restrictViews(View...)
 */
class ViewRestrict extends RestrictImpl<View> {

    ViewRestrict(View[] views) {
        super(views);
    }

    ViewRestrict(View view) {
        super(view);
    }

    @Override
    protected ObjectBasedPermissionAssignmentReverter<View> createReverter() {
        return new ViewObjectBasedPermissionAssignmentRegistration(this);
    }

    @Override
    protected void bindInternal() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        authorizationContext.ensureViewChangeListenerRegistered();

        for (Map.Entry<View, Set<Object>> entry : super.restrictionMap.entrySet()) {
            final View view = entry.getKey();
            final Set<Object> permissions = entry.getValue();

            authorizationContext.addPermissions(view, permissions);
        }
    }

}
