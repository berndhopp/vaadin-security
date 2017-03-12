package org.vaadin.security.api;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasFilterableDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.ui.Component;

import java.util.Set;

@SuppressWarnings("unused")
public interface Binder {

    //get permissions
    Set<Object> getPermissions(Component component);

    Set<Object> getViewPermissions(View view);

    //bind
    default Bind bindComponent(Component component) {
        return bindComponents(component);
    }

    Bind bindComponents(Component... component);

    default Bind bindView(View view) {
        return bindViews(view);
    }

    Bind bindViews(View... views);

    <T, F> Binder bindHasDataProvider(HasFilterableDataProvider<T, F> hasFilterableDataProvider);

    <T, F> Binder bindHasDataProvider(HasDataProvider<T> hasDataProvider);

    //unbind
    default Unbind unbindComponent(Component component) {
        return unbindComponents(component);
    }

    Unbind unbindComponents(Component... component);

    default Unbind unbindView(View view) {
        return unbindViews(view);
    }

    Unbind unbindViews(View... view);

    <T, F> boolean unbindHasDataProvider(HasFilterableDataProvider<T, F> hasFilterableDataProvider);

    <T, F> boolean unbindHasDataProvider(HasDataProvider<T> hasDataProvider);

    //fluent api
    interface Bind {
        Binder to(Object... permission);
    }

    interface Unbind {
        Binder from(Object... permissions);

        Binder fromAll();
    }
}
