package org.vaadin.security.api;

import com.vaadin.ui.Component;

@SuppressWarnings("unused")
public interface Applier {
    void applyAll();

    void apply(Component... components);
}
