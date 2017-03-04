package org.vaadin.security.api;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasFilterableDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.ui.Component;

import java.util.Set;

@SuppressWarnings("unused")
public interface Binder {

    Set<Object> getPermissions(Component component);

    Set<Object> getViewPermissions(View view);

    Bind bind(Component... component);

    Bind bindView(View... views);

    <T, F> Binder bind(Class<T> itemClass, HasFilterableDataProvider<T, F> hasFilterableDataProvider);

    <T, F> Binder bind(Class<T> itemClass, HasDataProvider<T> hasDataProvider);

    Unbind unbind(Component... component);

    Unbind unbindView(View... view);

    <T, F> boolean unbind(HasFilterableDataProvider<T, F> hasFilterableDataProvider);

    <T, F> boolean unbind(HasDataProvider<T> hasDataProvider);

    interface Bind {
        Binder to(Object... permission);
    }

    interface Unbind {
        Binder from(Object... permissions);

        Binder fromAll();
    }
}
