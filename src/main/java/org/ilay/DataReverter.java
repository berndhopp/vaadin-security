package org.ilay;

import com.vaadin.data.HasDataProvider;

import java.lang.ref.Reference;
import java.util.Objects;

class DataReverter extends OneTimeUsableReverter {

    private final Reference<HasDataProvider> hasDataProvider;

    DataReverter(Reference<HasDataProvider> hasDataProvider) {
        this.hasDataProvider = Objects.requireNonNull(hasDataProvider);
    }

    @Override
    void revertInternal() {

        final HasDataProvider hasDataProvider = this.hasDataProvider.get();

        if (hasDataProvider != null) {
            AuthorizationContext.getCurrent().unbindData(hasDataProvider);
        }
    }
}
