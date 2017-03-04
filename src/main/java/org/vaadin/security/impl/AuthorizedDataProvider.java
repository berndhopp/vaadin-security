package org.vaadin.security.impl;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.shared.Registration;
import org.vaadin.security.api.Evaluator;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

class AuthorizedDataProvider<T, F> implements DataProvider<T, F> {
    final DataProvider<T, F> dataProvider;
    private final Class<T> itemClass;
    private AuthorizationEngine authorizationEngine;

    AuthorizedDataProvider(AuthorizationEngine authorizationEngine, DataProvider<T, F> dataProvider, Class<T> itemClass) {
        this.authorizationEngine = authorizationEngine;
        this.dataProvider = dataProvider;
        this.itemClass = itemClass;
    }

    @Override
    public boolean isInMemory() {
        return dataProvider.isInMemory();
    }

    @Override
    public int size(Query<T, F> query) {
        return (int) fetch(query).count();
    }

    @Override
    public Stream<T> fetch(Query<T, F> query) {
        final Evaluator<T> evaluator = authorizationEngine.evaluatorPool.getEvaluator(itemClass);

        return dataProvider
                .fetch(query)
                .filter(evaluator::evaluate);
    }

    @Override
    public void refreshItem(T item) {
        final Evaluator<T> evaluator = authorizationEngine.evaluatorPool.getEvaluator(itemClass);

        checkArgument(evaluator.evaluate(item));

        dataProvider.refreshItem(item);
    }

    @Override
    public void refreshAll() {
        dataProvider.refreshAll();
    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<T> listener) {
        return dataProvider.addDataProviderListener(listener);
    }
}
