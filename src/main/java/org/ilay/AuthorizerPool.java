package org.ilay;

import org.ilay.api.Authorizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * the pool of available authorizers
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
class AuthorizerPool {

    private final Map<Class<?>, Authorizer<?, ?>> authorizers;

    AuthorizerPool(Collection<Authorizer> authorizers) {
        requireNonNull(authorizers);
        this.authorizers = new HashMap<>(authorizers.size());

        for (Authorizer authorizer : authorizers) {
            requireNonNull(authorizer);
            requireNonNull(authorizer.getPermissionClass());

            Authorizer<?, ?> alreadyRegistered = this.authorizers.put(authorizer.getPermissionClass(), authorizer);

            if (alreadyRegistered != null) {
                throw new ConflictingAuthorizersException(authorizer, alreadyRegistered, authorizer.getPermissionClass());
            }
        }
    }

    @SuppressWarnings("unchecked")
    <T, F> Authorizer<T, F> getAuthorizer(Class<T> permissionClass) {

        requireNonNull(permissionClass);

        Authorizer<T, F> authorizer = (Authorizer<T, F>) authorizers.get(permissionClass);

        if (authorizer != null) {
            return authorizer;
        }

        for (Authorizer<?, ?> anAuthorizer : authorizers.values()) {

            /**
             * in a sentence: a match is found if either the permission's class is an interface
             * that the authorizer's permission-class implements or if the permission's class
             * is a subclass of the authorizer's permission-class
             */
            boolean match = permissionClass.isInterface()
                    ? permissionClass.isAssignableFrom(anAuthorizer.getPermissionClass())
                    : anAuthorizer.getPermissionClass().isAssignableFrom(permissionClass);

            if (match) {
                if (authorizer != null) {
                    throw new ConflictingAuthorizersException(authorizer, anAuthorizer, permissionClass);
                }

                authorizer = (Authorizer<T, F>) anAuthorizer;
            }
        }

        Check.arg(authorizer != null, "no authorizer found for %s", permissionClass);

        authorizers.put(permissionClass, authorizer);

        return authorizer;
    }

    /**
     * if more than one authorizers are applicable for a given type, this exception is thrown
     *
     * @author Bernd Hopp bernd@vaadin.com
     */
    static class ConflictingAuthorizersException extends RuntimeException {
        ConflictingAuthorizersException(Authorizer authorizer1, Authorizer authorizer2, Class permissionClass) {
            super(
                    format(
                            "conflicting authorizers: %s and %s are both assignable to %s",
                            authorizer1,
                            authorizer2,
                            permissionClass)
            );
        }
    }
}
