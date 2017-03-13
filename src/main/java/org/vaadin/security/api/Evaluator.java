package org.vaadin.security.api;

/**
 * Evaluator is the object responsible of deciding if a certain permission is granted in the current
 * context or not. Usually the "current context" is the currently logged in user and it's roles. A
 * "permission" can be any object that is an instance of the generic type argument T, so that every
 * {@link Evaluator} is responsible for evaluating the permissions that are assignable to the type
 * T.
 *
 * @author Bernd Hopp
 */
@SuppressWarnings("unused")
public interface Evaluator<T> {

    /**
     * evaluate if a certain permission is granted in the current context
     *
     * @param permission the permission
     * @return true if the permission is granted, otherwise false
     */
    boolean evaluate(T permission);

    /**
     * returns the class of the permission that can be evaluated ( type-parameter T )
     *
     * @return the class of T
     */
    Class<T> getPermissionClass();
}
