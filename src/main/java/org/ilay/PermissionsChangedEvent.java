package org.ilay;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;

public class PermissionsChangedEvent extends ComponentEvent<UI> {
    public PermissionsChangedEvent() {
        super(UI.getCurrent(), false);
    }
}
