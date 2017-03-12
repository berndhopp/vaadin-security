package org.vaadin.security.impl;

import com.vaadin.server.SerializablePredicate;

class EvaluatorPredicate<T> implements SerializablePredicate<T> {
    private AuthorizationEngine authorizationEngine;

    EvaluatorPredicate(AuthorizationEngine authorizationEngine) {
        this.authorizationEngine = authorizationEngine;
    }

    @Override
    public boolean test(T permission) {
        return authorizationEngine.evaluate(permission);
    }
}
