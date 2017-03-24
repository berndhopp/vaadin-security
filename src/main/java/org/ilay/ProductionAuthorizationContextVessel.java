package org.ilay;

import com.vaadin.server.VaadinSession;

import static java.util.Objects.requireNonNull;

class ProductionAuthorizationContextVessel implements Vessel<AuthorizationContext> {
    @Override
    public AuthorizationContext get() {
        final VaadinSession vaadinSession = VaadinSession.getCurrent();

        requireNonNull(vaadinSession, "no VaadinSession available");

        final AuthorizationContext authorizationContext = vaadinSession.getAttribute(AuthorizationContext.class);

        return requireNonNull(
                authorizationContext,
                "no authorizationContext available in the current session, did you forget" +
                        "to call Authorization.start()?"
        );
    }

    @Override
    public void set(AuthorizationContext authorizationContext) {
        final VaadinSession vaadinSession = VaadinSession.getCurrent();

        requireNonNull(vaadinSession, "no VaadinSession available");

        Check.state(vaadinSession.getAttribute(AuthorizationContext.class) == null);

        vaadinSession.setAttribute(AuthorizationContext.class, authorizationContext);
    }
}
