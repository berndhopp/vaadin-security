package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderWrapper;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;

import org.ilay.api.Authorizer;
import org.ilay.api.DataAuthorizer;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * a special DataProviderWrapper to enable authorization-based filtering
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
class AuthorizingDataProvider<T, F, M> extends DataProviderWrapper<T, F, M> implements Predicate<T> {

    private static final long serialVersionUID = 4099563490669725218L;
    private final DataAuthorizer<T, M> authorizer;
    private final boolean integrityCheck;

    AuthorizingDataProvider(DataProvider<T, M> dataProvider, Authorizer<T> authorizer) {
        super(requireNonNull(dataProvider));
        requireNonNull(authorizer);

        if (authorizer instanceof DataAuthorizer) {
            this.authorizer = (DataAuthorizer<T, M>) authorizer;
            integrityCheck = true;
        } else {
            this.authorizer = new DataAuthorizer<T, M>() {
                @Override
                @SuppressWarnings("unchecked")
                public M asFilter() {
                    return (M) (SerializablePredicate<T>) authorizer::isGranted;
                }

                @Override
                public boolean isGranted(T permission) {
                    return authorizer.isGranted(permission);
                }

                @Override
                public Class<T> getPermissionClass() {
                    return authorizer.getPermissionClass();
                }
            };
            integrityCheck = false;
        }
    }

    @Override
    public Stream<T> fetch(Query<T, F> t) {
        Stream<T> stream = super.fetch(t);

        if (integrityCheck) {
            stream = stream.filter(this);
        }

        return stream;
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
