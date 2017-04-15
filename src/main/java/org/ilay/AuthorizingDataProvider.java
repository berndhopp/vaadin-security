package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderWrapper;
import com.vaadin.data.provider.Query;

import org.ilay.api.Authorizer;
import org.ilay.api.InMemoryAuthorizer;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * a special DataProviderWrapper to enable authorization-based filtering
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
class AuthorizingDataProvider<T, F, M> extends DataProviderWrapper<T, F, M> implements Predicate<T> {

    private final Authorizer<T, M> authorizer;
    private final boolean integrityCheck;

    AuthorizingDataProvider(DataProvider<T, M> dataProvider, Authorizer<T, M> authorizer) {
        super(requireNonNull(dataProvider));
        this.authorizer = requireNonNull(authorizer);

        //inMemory-DataProviders should use an InMemoryAuthorizer,
        //where an integrity check on the data would not make sense
        integrityCheck = !(authorizer instanceof InMemoryAuthorizer);
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
        Check.state(
                authorizer.isGranted(t),
                "item %s was not included by %s's filter, but permission to it was not granted by isGranted() method",
                t, authorizer
        );

        return true;
    }
}
