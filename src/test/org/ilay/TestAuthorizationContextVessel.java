package org.ilay;

import static java.util.Objects.requireNonNull;

class TestAuthorizationContextVessel implements Vessel<AuthorizationContext> {

    private AuthorizationContext authorizationContext;

    @Override
    public void set(AuthorizationContext authorizationContext) {
        Check.state(this.authorizationContext == null);
        this.authorizationContext = authorizationContext;
    }

    @Override
    public AuthorizationContext get() {
        return requireNonNull(authorizationContext);
    }
}
