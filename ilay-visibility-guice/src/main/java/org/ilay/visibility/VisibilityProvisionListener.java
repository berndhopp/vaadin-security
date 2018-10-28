package org.ilay.visibility;

import com.google.inject.spi.ProvisionListener;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

import org.ilay.PermissionsChangedEvent;

import static com.vaadin.flow.component.ComponentUtil.addListener;
import static org.ilay.visibility.VisibilityUtil.evaluateVisibility;

class VisibilityProvisionListener implements ProvisionListener {

    public <T> void onProvision(ProvisionInvocation<T> provision) {
        Component component = (Component) provision.provision();

        addListener(UI.getCurrent(), PermissionsChangedEvent.class, e -> evaluateVisibility(component));

        evaluateVisibility(component);
    }
}
