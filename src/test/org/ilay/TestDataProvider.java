package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.shared.Registration;

import java.util.Arrays;
import java.util.stream.Stream;

class TestDataProvider implements DataProvider<Foo, Bar> {

    private final Foo[] items;

    TestDataProvider(Foo... items) {
        this.items = items;
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<Foo, Bar> query) {
        return items.length;
    }

    @Override
    public Stream<Foo> fetch(Query<Foo, Bar> query) {
        return Arrays.stream(items);
    }

    @Override
    public void refreshItem(Foo item) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void refreshAll() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<Foo> listener) {
        throw new RuntimeException("not implemented");
    }
}
