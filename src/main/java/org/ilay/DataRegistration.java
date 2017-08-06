package org.ilay;

import com.vaadin.data.provider.DataProvider;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import static java.util.Objects.requireNonNull;

/**
 * reverts all operations on 'data' ( instead of 'objects' ), like Authorization.restrictData()
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
class DataRegistration<T> extends RegistrationBase {

    private static final long serialVersionUID = 388532996079070924L;
    private final Reference<Holder<DataProvider<T, ?>>> reference;

    DataRegistration(Holder<DataProvider<T, ?>> hasDataProvider) {
        requireNonNull(hasDataProvider);
        this.reference = new WeakReference<>(hasDataProvider);
    }

    @Override
    void revertInternal() {

        final Holder<DataProvider<T, ?>> dataProviderHolder = this.reference.get();

        if (dataProviderHolder != null) {
            AuthorizationContext.getCurrent().unbindData(dataProviderHolder);
        }
    }
}
