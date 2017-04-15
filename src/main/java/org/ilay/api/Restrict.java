package org.ilay.api;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A Restrict is the first of two steps of the fluent api to bind
 * components and views to permissions. After a Restrict is
 * obtained from {@link org.ilay.Authorization}, either
 * {@link Restrict#to(Object)} or {@link Restrict#to(Object...)}
 * need to be called, otherwise the authorization-framework
 * will throw an exception at the next interaction.
 *
 * <code>
 * Authorization.restrictComponent(myAdminButton).to("admin");
 *
 * // if "admin" is not granted, myAdmminButton will be invisible
 * </code>
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
public interface Restrict {
    /**
     * restricts the {@link com.vaadin.ui.Component} or {@link com.vaadin.navigator.View}
     * to the given permission. An applicable {@link Authorizer} must be available
     * for the type of the permission.
     * @see org.ilay.Authorization#start(Set)
     * @see org.ilay.Authorization#start(Supplier)
     * @param permission the object that represents a permission needed for the component to be visible
     *                   or the view to be navigable
     * @return a {@link Reverter} to remove the permission from the component or view, so that it is no
     *                            longer needed to be granted in order for the component to be visible
     *                            or the view to be navigable
     */
    Reverter to(Object permission);

    /**
     * restricts the {@link com.vaadin.ui.Component} or {@link com.vaadin.navigator.View}
     * to the given permission. An applicable {@link Authorizer} must be available
     * for the type of the permission.
     * @see org.ilay.Authorization#start(Set)
     * @see org.ilay.Authorization#start(Supplier)
     * @param permissions the objects that represents permissions needed to be granted
     *                    for the component to be visible or the view to be navigable
     * @return a {@link Reverter} to remove the permissions from the component or view, so that they are
     *                            no longer needed to be granted in order for the component to be visible
     *                            or the view to be navigable
     */
    Reverter to(Object... permissions);
}
