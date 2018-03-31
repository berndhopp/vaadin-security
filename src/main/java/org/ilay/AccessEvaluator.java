package org.ilay;

import com.vaadin.flow.router.Location;

public interface AccessEvaluator {
    Access evaluate(Location location, Class<?> navigationTarget);
}
