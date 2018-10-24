package org.ilay.visibility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface VisibilityAnnotation {
    /**
     * The {@link VisibilityEvaluator} that is to be assigned to the annotation.
     *
     * @return the {@link VisibilityEvaluator}
     */
    Class<? extends VisibilityEvaluator<?, ?>> value();
}
