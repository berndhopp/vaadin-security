package org.ilay.visibility;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.ListenerPriority;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinServiceInitListener;

/**
 * internally used class, do not touch
 */
@ListenerPriority(Integer.MAX_VALUE - 2)
public final class VisibilityEngine implements VaadinServiceInitListener, UIInitListener, AfterNavigationListener {

    private static final long serialVersionUID = 7808168756398878583L;

    @Override
    public void serviceInit(ServiceInitEvent e) {
        e.getSource().addUIInitListener(this);
    }

    @Override
    public void uiInit(UIInitEvent event) {
        event.getUI().addAfterNavigationListener(this);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        UI.getCurrent().getChildren().forEach(VisibilityUtil::evaluateVisibility);
    }
}


