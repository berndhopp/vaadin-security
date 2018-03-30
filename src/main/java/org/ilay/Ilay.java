package org.ilay;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

import static java.util.Objects.requireNonNull;

class Ilay implements VaadinServiceInitListener, UIInitListener, BeforeEnterListener {

    private static final long serialVersionUID = 8512687361151475367L;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(this);
    }

    @Override
    public void uiInit(UIInitEvent event) {
        event.getUI().addBeforeEnterListener(this);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Class<?> navigationTarget = event.getNavigationTarget();

        if (navigationTarget.isAnnotationPresent(Restricted.class)) {
            final Restricted restricted = navigationTarget.getAnnotation(Restricted.class);

            final VaadinService vaadinService = requireNonNull(VaadinService.getCurrent());

            final Instantiator instantiator = requireNonNull(vaadinService.getInstantiator());

            final AccessEvaluator accessEvaluator = requireNonNull(instantiator.getOrCreate(restricted.value()));

            final Access access = requireNonNull(
                    accessEvaluator.evaluate(event.getLocation(), event.getNavigationTarget(), event.getRouteTargetType()),
                    () -> restricted.value() + "#checkAccess(BeforeEnterEvent) must not return null"
            );

            access.ifRestricted(event::rerouteTo);
        }
    }
}
