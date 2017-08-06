package org.ilay;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasFilterableDataProvider;
import com.vaadin.data.provider.DataProvider;

interface Holder<T> {

    static <U> Holder<DataProvider<U, ?>> wrap(HasDataProvider<U> hasDataProvider) {
        return new Holder<DataProvider<U, ?>>() {
            @Override
            public DataProvider<U, ?> get() {
                return hasDataProvider.getDataProvider();
            }

            @Override
            public void set(DataProvider<U, ?> dataProvider) {
                hasDataProvider.setDataProvider(dataProvider);
            }
        };
    }

    static <U, V> Holder<DataProvider<U, V>> wrap(HasFilterableDataProvider<U, V> hasDataProvider) {
        return new Holder<DataProvider<U, V>>() {
            @Override
            @SuppressWarnings("unchecked")
            public DataProvider<U, V> get() {
                return (DataProvider<U, V>) hasDataProvider.getDataProvider();
            }

            @Override
            public void set(DataProvider<U, V> dataProvider) {
                hasDataProvider.setDataProvider(dataProvider);
            }
        };
    }

    T get();

    void set(T t);
}
