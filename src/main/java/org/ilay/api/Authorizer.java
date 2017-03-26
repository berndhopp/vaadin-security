package org.ilay.api;

import java.util.Optional;

/**
 * Authorizer is the object responsible of deciding if a certain permission is granted in the current
 * context or not. Usually the "current context" is the currently logged in user and it's roles. A
 * "permission" can be any object that is an instance of the generic type argument T, so that every
 * {@link Authorizer} is responsible for evaluating the permissions that are assignable to the type
 * T.
 *
 * @author Bernd Hopp
 */
public interface Authorizer<T, F> {

    /**
     * evaluate if a certain permission is granted in the current context
     *
     * @param permission the permission
     * @return true if the permission is granted, otherwise false
     */
    boolean isGranted(T permission);

    /**
     * returns the class of the permission that can be evaluated ( type-parameter T )
     *
     * @return the class of T
     */
    Class<T> getPermissionClass();

    /**
     * the filter to be used for DataProviders, see {@link com.vaadin.data.provider.DataProvider}
     */
    F asFilter();

    /**
     * returns the class of the filter if implemented, otherwise Optional.empty()
     *
     * @return an {@link Optional} containing the {@link Class} of the filter or Optional.empty()
     */
    default Optional<Class<F>> getFilterClass() {
        return Optional.empty();
    }
}
