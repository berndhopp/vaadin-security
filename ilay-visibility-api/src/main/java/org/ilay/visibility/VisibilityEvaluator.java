package org.ilay.visibility;

import java.lang.annotation.Annotation;

public interface VisibilityEvaluator<ANNOTATION extends Annotation> {
    boolean evaluateVisibility(ANNOTATION annotation);
}
