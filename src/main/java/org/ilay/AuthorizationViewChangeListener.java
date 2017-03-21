package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public class AuthorizationViewChangeListener implements ViewChangeListener {

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        requireNonNull(event);

        final View newView = event.getNewView();

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        final Map<View, Collection<Object>> viewsToPermissions = authorizationContext.getViewsToPermissions();

        final Collection<Object> permissions = viewsToPermissions.get(newView);

        return permissions == null || permissions.stream().allMatch(authorizationContext::evaluate);
    }
}
