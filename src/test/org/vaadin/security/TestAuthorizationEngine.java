package org.vaadin.security;

import com.vaadin.navigator.Navigator;

import org.vaadin.security.api.EvaluatorPool;

public class TestAuthorizationEngine extends AuthorizationEngine {
    public TestAuthorizationEngine(EvaluatorPool evaluatorPool) {
        super(evaluatorPool, false);
    }

    @Override
    Navigator getNavigator() {
        return null;
    }
}
