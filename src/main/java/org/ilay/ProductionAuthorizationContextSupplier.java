package org.ilay;

import com.vaadin.server.VaadinSession;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class ProductionAuthorizationContextSupplier implements Supplier<AuthorizationContext> {
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
}
