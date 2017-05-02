package org.ilay.api;

/**
 * Authorizer is the object responsible of deciding if a certain permission is granted in the
 * current context or not. Usually the "current context" is the currently logged in user and it's
 * roles. A "permission" can be any object that is an instance of the generic type argument T, so
 * that every {@link Authorizer} is responsible for evaluating the permissions that are assignable
 * to the type T.
 *
 * If the type T is not used in any {@link com.vaadin.data.provider.DataProvider} that is not a
 * {@link com.vaadin.data.provider.ListDataProvider}, i.e. in all cases where no filtering on a
 * backend-level like an external database is involved, consider using {@link InMemoryAuthorizer}.
 *
 * @author Bernd Hopp bernd@vaadin.com
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
}
