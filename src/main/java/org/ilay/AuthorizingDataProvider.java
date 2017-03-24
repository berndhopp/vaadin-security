package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderWrapper;
import com.vaadin.data.provider.Query;

import java.util.function.Predicate;
import java.util.stream.Stream;

class AuthorizingDataProvider<T, F, M> extends DataProviderWrapper<T, F, M> implements Predicate<T> {

    private final Authorizer<T, M> authorizer;
    private final boolean integrityCheck;

    AuthorizingDataProvider(DataProvider<T, M> dataProvider, Authorizer<T, M> authorizer, boolean integrityCheck) {
        super(dataProvider);
        this.authorizer = authorizer;
        this.integrityCheck = integrityCheck;
    }

    @Override
    public Stream<T> fetch(Query<T, F> t) {

        if (integrityCheck) {
            return super.fetch(t).filter(this);
        } else {
            return super.fetch(t);
        }
    }

    @Override
    protected M getFilter(Query<T, F> query) {
        return authorizer.asFilter();
    }

    DataProvider<T, M> getWrappedDataProvider() {
        return super.dataProvider;
    }

    @Override
    public boolean test(T t) {
        if (!authorizer.isGranted(t)) {
            //if we get here, filter ( M ) and Authorizer.isGranted() do not work in sync correctly
            throw new AuthorizationException();
        }

        return true;
    }
}
