package org.ilay;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

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
                throw new ConflictingEvaluatorsException(authorizer, alreadyRegistered, authorizer.getPermissionClass());
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

            //TODO this needs explanation, as soon as I can wrap my own head around it
            boolean match = permissionClass.isInterface()
                    ? permissionClass.isAssignableFrom(anAuthorizer.getPermissionClass())
                    : anAuthorizer.getPermissionClass().isAssignableFrom(permissionClass);

            if (match) {
                if (authorizer != null) {
                    throw new ConflictingEvaluatorsException(authorizer, anAuthorizer, permissionClass);
                }

                authorizer = (Authorizer<T, F>) anAuthorizer;
            }
        }

        Check.arg(authorizer != null, "no authorizer found for %s", permissionClass);

        authorizers.put(permissionClass, authorizer);

        return authorizer;
    }
}
