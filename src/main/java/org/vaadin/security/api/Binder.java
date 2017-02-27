package org.vaadin.security.api;

import com.vaadin.navigator.View;
import com.vaadin.ui.Component;

import java.util.Set;

@SuppressWarnings("unused")
public interface Binder {

    Set<Object> getPermissions(Component component);
    Set<Object> getViewPermissions(View view);

    Bind bind(Component... component);
    Bind bindView(View... views);

    Unbind unbind(Component... component);
    Unbind unbindView(View... view);

    interface Bind {
        Binder to(Object... permission);
    }

    interface Unbind {
        Binder from(Object... permissions);
        Binder fromAll();
    }
}
