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
            if (anAuthorizer.getPermissionClass().isAssignableFrom(permissionClass)) {
                if (authorizer != null) {
                    throw new ConflictingEvaluatorsException(authorizer, anAuthorizer, permissionClass);
                }

                authorizer = (Authorizer<T, F>) anAuthorizer;
            }
        }

        requireNonNull(authorizer, "no authorizer found for " + permissionClass);

        authorizers.put(permissionClass, authorizer);

        return authorizer;
    }
}
