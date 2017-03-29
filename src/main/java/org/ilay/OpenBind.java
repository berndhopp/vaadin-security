package org.ilay;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

final class OpenBind implements Supplier<Authorization.HasSet> {

    static VaadinAbstraction.Vessel<OpenBind> openBindVessel = new VaadinAbstraction.ProductionOpenBindVessel();
    private final Authorization.HasSet hasSet;

    OpenBind(Authorization.HasSet hasSet) {
        this.hasSet = hasSet;
    }

    static Optional<Authorization.HasSet> getCurrent() {
        final OpenBind openBind = openBindVessel.get();

        if (openBind != null) {
            return Optional.of(openBind.get());
        } else {
            return Optional.empty();
        }
    }

    static void setCurrent(Authorization.HasSet hasSet) {
        Check.noOpenBind();
        requireNonNull(hasSet);

        OpenBind openBind = new OpenBind(hasSet);

        openBindVessel.set(openBind);
    }

    static void unsetCurrent() {
        Check.state(getCurrent().isPresent());

        openBindVessel.set(null);
    }

    @Override
    public Authorization.HasSet get() {
        return hasSet;
    }
}
