package org.ilay;

import com.vaadin.flow.router.Location;

import java.lang.annotation.Annotation;

public interface AccessEvaluator<T extends Annotation> {
    Access evaluate(Location location, Class<?> navigationTarget, T annotation);
}
