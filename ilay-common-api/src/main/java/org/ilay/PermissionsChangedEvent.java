package org.ilay;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

/**
 * Fired when permissions have changed. This is usually the case on login/logout, or when the
 * current user has been granted or revoked certain permissions.
 */
public class PermissionsChangedEvent extends ComponentEvent<UI> {
    private static final long serialVersionUID = -668093721644293986L;

    public PermissionsChangedEvent() {
        super(UI.getCurrent(), false);
    }
}
