package org.vaadin.security.api;

import com.vaadin.ui.Component;

/**
 *
 */
@SuppressWarnings("unused")
public interface Binder {
    Bind bind(Component... component);

    Unbind unbind(Component... component);

    interface Bind {
        BindTerminate to(Object... permission);
    }

    interface Unbind {
        BindTerminate from(Object... permissions);
        BindTerminate fromAll();
    }

    interface BindTerminate {
        Binder and();
    }
}
