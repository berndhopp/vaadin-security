package org.ilay;

import com.vaadin.navigator.View;

import java.util.Map;
import java.util.Set;

/**
 * @see {@link Authorization#restrictView(View)}
 * @see {@link Authorization#restrictViews(View...)}
 */
class ViewRestrict extends RestrictImpl<View> {
    ViewRestrict(View[] views) {
        super(views);
    }

    ViewRestrict(View view) {
        super(view);
    }

    @Override
    protected ObjectsRegistration<View> asRegistration() {
        return new ViewObjectsRegistration(this);
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
