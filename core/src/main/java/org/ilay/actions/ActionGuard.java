package org.ilay.actions;

import java.lang.annotation.Annotation;

public interface ActionGuard<ANNOTATION extends Annotation> {
    void ifAllowed(ANNOTATION annotation, Runnable runnable);
}
