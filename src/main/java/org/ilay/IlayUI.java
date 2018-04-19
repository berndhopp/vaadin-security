package org.ilay;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.server.VaadinService;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public class IlayUI extends UI {

    private static final long serialVersionUID = 974589421761348380L;

    public IlayUI() {
        addBeforeEnterListener(event -> {
            final Class<?> navigationTarget = event.getNavigationTarget();

            final Restricted restricted = navigationTarget.getAnnotation(Restricted.class);

            if (restricted != null) {
                final VaadinService vaadinService = requireNonNull(VaadinService.getCurrent());

                final Instantiator instantiator = requireNonNull(vaadinService.getInstantiator());

                final AccessEvaluator accessEvaluator = requireNonNull(instantiator.getOrCreate(restricted.value()));

                final Access access = requireNonNull(
                        accessEvaluator.evaluate(event.getLocation(), navigationTarget),
                        () -> restricted.value() + "#checkAccess(BeforeEnterEvent) must not return null"
                );

                access.exec(event);
            }
        });
    }
}
