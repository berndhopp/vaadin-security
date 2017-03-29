package org.ilay;

import com.vaadin.navigator.View;

import java.util.Set;

/**
 * @see {@link Authorization#bindView(View)}
 * @see {@link Authorization#bindViews(View...)}
 */
class ViewBind extends Authorization.Bind<View> {
    ViewBind(View[] views) {
        super(views);
    }

    ViewBind(View view) {
        super(view);
    }

    @Override
    protected void bindInternal(Set<Object> permissions) {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        authorizationContext.ensureViewChangeListenerRegistered();

        for (View view : super.tSet) {
            authorizationContext.addPermissions(view, permissions);
        }
    }
}
