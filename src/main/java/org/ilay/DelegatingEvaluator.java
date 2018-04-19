package org.ilay;

import com.vaadin.flow.router.Location;
import com.vaadin.flow.server.VaadinService;

class DelegatingEvaluator implements AccessEvaluator<Restricted> {
    @Override
    @SuppressWarnings("unchecked")
    public Access evaluate(Location location, Class navigationTarget, Restricted annotation) {

        AccessEvaluator accessEvaluator = VaadinService.getCurrent().getInstantiator().getOrCreate(annotation.value());

        return accessEvaluator.evaluate(location, navigationTarget, annotation);
    }
}
