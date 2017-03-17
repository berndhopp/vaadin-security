package org.vaadin.authorization;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializableBiFunction;
import com.vaadin.server.SerializableFunction;
import com.vaadin.shared.Registration;

import java.util.stream.Stream;

class DataProviderWrapper<T, F> implements DataProvider<T, F> {
    private final DataProvider<T, F> dataProvider;
    private AuthorizationContext authorizationContext;

    DataProviderWrapper(AuthorizationContext authorizationContext, DataProvider<T, F> dataProvider) {
        this.authorizationContext = authorizationContext;
        this.dataProvider = dataProvider;
    }

    @Override
    public boolean isInMemory() {
        return dataProvider.isInMemory();
    }

    @Override
    public int size(Query<T, F> query) {
        return dataProvider.size(query);
    }

    @Override
    public Stream<T> fetch(Query<T, F> query) {
        return dataProvider.fetch(query).filter(
                item -> {
                    if (!authorizationContext.evaluate(item)) {
                        throw new IllegalArgumentException();
                    }

                    return true;
                }
        );
    }

    @Override
    public void refreshItem(T item) {
        if (!authorizationContext.evaluate(item)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void refreshAll() {
        dataProvider.refreshAll();
    }

    @Override
    public Object getId(T item) {
        if (!authorizationContext.evaluate(item)) {
            throw new IllegalArgumentException();
        }

        return dataProvider.getId(item);
    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<T> listener) {
        return dataProvider.addDataProviderListener(listener);
    }

    @Override
    public <C> DataProvider<T, C> withConvertedFilter(SerializableFunction<C, F> filterConverter) {
        return dataProvider.withConvertedFilter(filterConverter);
    }

    @Override
    public <Q, C> ConfigurableFilterDataProvider<T, Q, C> withConfigurableFilter(SerializableBiFunction<Q, C, F> filterCombiner) {
        return dataProvider.withConfigurableFilter(filterCombiner);
    }

    @Override
    public ConfigurableFilterDataProvider<T, Void, F> withConfigurableFilter() {
        return dataProvider.withConfigurableFilter();
    }
}
