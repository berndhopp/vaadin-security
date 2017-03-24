package org.ilay;

import com.vaadin.server.VaadinService;

import java.util.function.Supplier;

class ProductionSessionInitNotifierSupplier implements Supplier<SessionInitNotifier> {
    @Override
    public SessionInitNotifier get() {
        final VaadinService vaadinService = VaadinService.getCurrent();

        if (vaadinService == null) {
            throw new IllegalStateException("VaadinService is not initialized yet");
        }

        return vaadinService::addSessionInitListener;
    }
}
