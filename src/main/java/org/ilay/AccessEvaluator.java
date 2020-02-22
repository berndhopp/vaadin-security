package org.ilay;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;

import java.lang.annotation.Annotation;

/**
 * An {@link AccessEvaluator} determines whether the current user has access to a certain
 * route-target or not. Instances of this class will be constructed by a dependency-injection
 * framework like spring or guice, if one is used.
 */
public interface AccessEvaluator {

    /**
     * evaluate what access the current user has to the route-target in question.
     *
     * @param beforeEnterEvent the {@link BeforeEnterEvent} that is to be evaluated for access
     *
     * @return the {@link Access}
     */
    Access evaluate(BeforeEnterEvent beforeEnterEvent);
}
