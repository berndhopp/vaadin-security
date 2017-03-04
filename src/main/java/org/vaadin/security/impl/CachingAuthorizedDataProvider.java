package org.vaadin.security.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilderSpec;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.cache.CacheBuilder.from;
import static java.util.stream.Collectors.toList;

class CachingAuthorizedDataProvider<T, F> extends AuthorizedDataProvider<T, F> {

    private final Cache<Query<T, F>, List<T>> streamCache;

    CachingAuthorizedDataProvider(AuthorizationEngine authorizationEngine, DataProvider<T, F> dataProvider, Class<T> itemClass, CacheBuilderSpec cacheBuilderSpec) {
        super(authorizationEngine, dataProvider, itemClass);

        streamCache = from(cacheBuilderSpec).build();
    }

    @Override
    public Stream<T> fetch(Query<T, F> query) {
        if (query == null) {
            return super.fetch(null);
        }

        List<T> list = streamCache.getIfPresent(query);

        if (list == null) {
            Stream<T> stream = super.fetch(query);

            if (stream == null) {
                return null;
            }

            list = stream.collect(toList());

            streamCache.put(query, list);
        }

        return list.stream();
    }
}
