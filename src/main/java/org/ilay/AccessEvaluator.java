package org.ilay;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;

import java.lang.annotation.Annotation;

/**
 * An {@link AccessEvaluator} determines whether the current user has access to a certain
 * route-target or not. Instances of this class will be constructed by a dependency-injection
 * framework like spring or guice, if one is used.
 */
public interface AccessEvaluator<T extends Annotation> {

    /**
     * evaluate what access the current user has to the route-target in question.
     *
     * @param location         the {@link Location} to be navigated to, see {@link
     *                         BeforeEnterEvent#getLocation()}
     * @param navigationTarget the navigation-target to be navigated to, see {@link
     *                         BeforeEnterEvent#getNavigationTarget()}
     * @param annotation       the {@link Annotation} on the route-target that itself is annotated
     *                         with a {@link NavigationAnnotation}. This annotation may carry
     *                         additional data which can be used to evaluate the access.
     * @return the {@link Access}
     */
    Access evaluate(Location location, Class<?> navigationTarget, T annotation);
}
