package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.shared.Registration;

import java.util.stream.Stream;

public class TestFilterableDataProvider implements DataProvider<Foo, Bar> {

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<Foo, Bar> query) {
        return 0;
    }

    @Override
    public Stream<Foo> fetch(Query<Foo, Bar> query) {
        return null;
    }

    @Override
    public void refreshItem(Foo foo) {

    }

    @Override
    public void refreshAll() {

    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<Foo> dataProviderListener) {
        return null;
    }
}
