package org.ilay.visibility;

import com.vaadin.flow.component.Component;

import java.lang.annotation.Annotation;

public interface VisibilityEvaluator<COMPONENT extends Component, ANNOTATION extends Annotation> {

    boolean evaluateVisibility(COMPONENT component, ANNOTATION annotation);
}
