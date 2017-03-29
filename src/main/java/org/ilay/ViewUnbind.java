package org.ilay;

import com.vaadin.navigator.View;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @see {@link Authorization#unbindView(View)}
 * @see {@link Authorization#unbindViews(View...)}
 */
class ViewUnbind extends Authorization.Unbind<View> {

    ViewUnbind(View[] tArray) {
        super(tArray);
    }

    ViewUnbind(View view) {
        super(view);
    }

    protected void unbindInternal(Set<Object> permissions) {

        final Map<View, Set<Object>> viewsToPermissions = AuthorizationContext
                .getCurrent()
                .getViewsToPermissions();

        super.tSet
                .stream()
                .map(viewsToPermissions::get)
                .filter(Objects::nonNull)
                .forEach(viewPermissions -> viewPermissions.removeAll(permissions));
    }

    public void fromAll() {
        final Map<View, Set<Object>> viewsToPermissions = AuthorizationContext.getCurrent()
                .getViewsToPermissions();

        super.tSet.forEach(viewsToPermissions::remove);
    }
}
