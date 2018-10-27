package org.ilay.guice;

import com.google.inject.Binding;
import com.google.inject.matcher.AbstractMatcher;

import com.vaadin.flow.component.Component;

import org.ilay.visibility.VisibilityAnnotation;

import java.lang.annotation.Annotation;

import static java.util.Arrays.stream;

class VisibilityAnnotationMatcher extends AbstractMatcher<Binding<?>> {
    public boolean matches(Binding<?> binding) {
        final Class<?> rawType = binding.getKey().getTypeLiteral().getRawType();

        final Annotation[] annotations = rawType.getAnnotations();

        final int visibilityAnnotationsCount = (int) stream(annotations)
                .map(Annotation::annotationType)
                .filter(at -> at.isAnnotationPresent(VisibilityAnnotation.class))
                .count();

        switch (visibilityAnnotationsCount) {
            case 0:
                return false;
            case 1: {
                if (!Component.class.isAssignableFrom(rawType)) {
                    throw new IllegalStateException(rawType + " has visibilityAnnotation but is not a component");
                }
                return true;
            }
            default:
                throw new IllegalStateException("more than one visibilityAnnotation found at " + rawType);
        }
    }
}
