package org.vaadin.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation binds vaadin-security to Dependency-Injection frameworks like Spring, Guice or Java CDI.
 * It may be attached to a {@link com.vaadin.ui.Component} or a {@link com.vaadin.navigator.View} that needs to
 * have restricted access. Access-grant is evaluated by the the configured {@link org.vaadin.security.api.PermissionEvaluator#hasPermission(String)}
 * which gets passed the {@link Restricted#value()}. Without granted permission for {@link Restricted#value()},
 * views will not be navigable and components will be invisible.
 *
 * See the implementation ( Spring, Guice, CDI ) for details how to set up {@link org.vaadin.security.api.PermissionEvaluator}
 *
 * @author Bernd Hopp
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Restricted {
    /**
     * the permission needed to see the component or access the view
     */
    String value();
}
