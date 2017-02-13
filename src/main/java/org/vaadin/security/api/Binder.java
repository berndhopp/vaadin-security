package org.vaadin.security.api;

import com.vaadin.navigator.View;
import com.vaadin.ui.Component;

@SuppressWarnings("unused")
public interface Binder {
    Bind bind(Component... component);
    Bind bindView(View... views);

    Unbind unbind(Component... component);
    Unbind unbindView(View... view);

    interface Bind {
        BindTerminate to(Object... permission);
    }

    interface Unbind {
        BindTerminate from(Object... permissions);
        BindTerminate fromAll();
    }

    interface BindTerminate {
        Binder then();
    }
}
