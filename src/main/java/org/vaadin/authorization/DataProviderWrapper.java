package org.vaadin.authorization;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializableBiFunction;
import com.vaadin.server.SerializableFunction;
import com.vaadin.shared.Registration;

import java.util.function.Predicate;
import java.util.stream.Stream;

class DataProviderWrapper<T, F> implements DataProvider<T, F> {
    private final DataProvider<T, F> wrappedDataProvider;
    private final AuthorizationContext authorizationContext;

    DataProviderWrapper(AuthorizationContext authorizationContext, DataProvider<T, F> wrappedDataProvider) {
        this.authorizationContext = authorizationContext;
        this.wrappedDataProvider = wrappedDataProvider;
    }

    @Override
    public boolean isInMemory() {
        return wrappedDataProvider.isInMemory();
    }

    @Override
    public int size(Query<T, F> query) {
        return wrappedDataProvider.size(query);
    }

    @Override
    public Stream<T> fetch(Query<T, F> query) {

        final Predicate<T> itemAuthenticationListener = item -> {
            if (!authorizationContext.evaluate(item)) {
                throw new IllegalArgumentException();
            }

            return true;
        };

        return wrappedDataProvider
                .fetch(query)
                .filter(itemAuthenticationListener);
    }

    @Override
    public void refreshItem(T item) {
        if (!authorizationContext.evaluate(item)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void refreshAll() {
        wrappedDataProvider.refreshAll();
    }

    @Override
    public Object getId(T item) {
        if (!authorizationContext.evaluate(item)) {
            throw new IllegalArgumentException();
        }

        return wrappedDataProvider.getId(item);
    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<T> listener) {
        return wrappedDataProvider.addDataProviderListener(listener);
    }

    @Override
    public <C> DataProvider<T, C> withConvertedFilter(SerializableFunction<C, F> filterConverter) {
        return wrappedDataProvider.withConvertedFilter(filterConverter);
    }

    @Override
    public <Q, C> ConfigurableFilterDataProvider<T, Q, C> withConfigurableFilter(SerializableBiFunction<Q, C, F> filterCombiner) {
        return wrappedDataProvider.withConfigurableFilter(filterCombiner);
    }

    @Override
    public ConfigurableFilterDataProvider<T, Void, F> withConfigurableFilter() {
        return wrappedDataProvider.withConfigurableFilter();
    }
}
