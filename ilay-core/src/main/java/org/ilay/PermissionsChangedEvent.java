package org.ilay;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class PermissionsChangedEvent extends ComponentEvent<UI> {
    private static final long serialVersionUID = -668093721644293986L;

    public PermissionsChangedEvent() {
        super(UI.getCurrent(), false);
    }
}
