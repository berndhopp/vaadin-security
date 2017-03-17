package org.vaadin.authorization;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderWrapper;
import com.vaadin.data.provider.Query;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

class AuthorizingDataProviderWrapper<T, F> extends DataProviderWrapper<T, F, F> {
    private final DataProvider<T, F> wrappedDataProvider;
    private final Authorization.BackendEvaluator<T, F> evaluator;
    private final Predicate<T> itemTest;

    AuthorizingDataProviderWrapper(DataProvider<T, F> wrappedDataProvider, Authorization.BackendEvaluator<T, F> evaluator) {
        super(wrappedDataProvider);
        this.evaluator = Objects.requireNonNull(evaluator);
        this.wrappedDataProvider = Objects.requireNonNull(wrappedDataProvider);
        itemTest = i -> {
            if (!evaluator.evaluate(i)) {
                throw new IllegalStateException();
            }

            return true;
        };
    }

    DataProvider<T, F> getWrappedDataProvider() {
        return wrappedDataProvider;
    }

    @Override
    public Stream<T> fetch(Query<T, F> t) {
        return super.fetch(t).filter(itemTest);
    }

    @Override
    protected F getFilter(Query<T, F> query) {
        return evaluator.getFilter();
    }
}
