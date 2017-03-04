package org.vaadin.security.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasFilterableDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.vaadin.security.api.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.*;
import static com.google.common.cache.CacheBuilder.from;
import static com.google.common.collect.ImmutableSet.copyOf;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
public class AuthorizationEngine implements Binder, Applier, ViewGuard {

    private static boolean setUp = false;
    final Multimap<Component, Object> componentsToPermissions = HashMultimap.create();
    final Multimap<View, Object> viewsToPermissions = HashMultimap.create();
    private final CacheBuilderSpec cacheBuilderSpec;
    private final EvaluatorPool evaluatorPool;
    private final Set<HasDataProvider<?>> boundHasDataProviders = new HashSet<>();
    private final Set<HasFilterableDataProvider<?, ?>> boundHasFilteredDataProviders = new HashSet<>();
    private final Object2BooleanMap<Component> componentsToLastKnownVisibilityState;
    private final boolean allowManualSettingOfVisibility;

    AuthorizationEngine(EvaluatorPool evaluatorPool, boolean allowManualSettingOfVisibility, CacheBuilderSpec cacheBuilderSpec) {
        this.allowManualSettingOfVisibility = allowManualSettingOfVisibility;
        this.evaluatorPool = checkNotNull(evaluatorPool);

        componentsToLastKnownVisibilityState = allowManualSettingOfVisibility
                ? null
                : new Object2BooleanOpenHashMap<>();

        this.cacheBuilderSpec = cacheBuilderSpec;
    }

    public static void start(Supplier<EvaluatorPool> evaluatorPoolSupplier) {
        start(evaluatorPoolSupplier, false, null);
    }

    public static void start(Supplier<EvaluatorPool> evaluatorPoolSupplier, CacheBuilderSpec cacheBuilderSpec) {
        start(evaluatorPoolSupplier, false, cacheBuilderSpec);
    }

    public static void start(Supplier<EvaluatorPool> evaluatorPoolSupplier, boolean allowManualSettingOfVisibility) {
        start(evaluatorPoolSupplier, allowManualSettingOfVisibility, null);
    }

    public static void start(Supplier<EvaluatorPool> evaluatorPoolSupplier, boolean allowManualSettingOfVisibility, CacheBuilderSpec cacheBuilderSpec) {

        checkState(!setUp, "setUp() cannot be called more than once");
        checkNotNull(evaluatorPoolSupplier);

        VaadinService.getCurrent().addSessionInitListener(
                event -> {
                    final EvaluatorPool evaluatorPool = checkNotNull(evaluatorPoolSupplier.get());

                    AuthorizationEngine authorizationEngine = new AuthorizationEngine(evaluatorPool, allowManualSettingOfVisibility, cacheBuilderSpec);

                    final VaadinSession session = event.getSession();

                    session.setAttribute(Binder.class, authorizationEngine);
                    session.setAttribute(Applier.class, authorizationEngine);
                    session.setAttribute(ViewGuard.class, authorizationEngine);
                }
        );

        setUp = true;
    }

    @Override
    public Set<Object> getPermissions(Component component) {
        checkNotNull(component);
        return copyOf(componentsToPermissions.get(component));
    }

    @Override
    public Set<Object> getViewPermissions(View view) {
        checkNotNull(view);
        return copyOf(viewsToPermissions.get(view));
    }

    @Override
    public Bind bind(Component... components) {
        checkNotNull(components);
        checkArgument(components.length > 0);
        return new BindImpl(this, components);
    }

    @Override
    public Bind bindView(View... views) {
        checkNotNull(views);
        checkArgument(views.length > 0);
        return new ViewBindImpl(this, views);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, F> Binder bind(Class<T> itemClass, HasDataProvider<T> hasDataProvider) {
        checkNotNull(itemClass);
        checkNotNull(hasDataProvider);

        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasDataProvider.getDataProvider();

        checkNotNull(dataProvider);

        hasDataProvider.setDataProvider(
                cacheBuilderSpec != null
                        ? new CachingAuthorizedDataProvider<>(dataProvider, itemClass, cacheBuilderSpec)
                        : new AuthorizedDataProvider<>(dataProvider, itemClass)
        );

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> Binder bind(Class<T> itemClass, HasFilterableDataProvider<T, F> hasFilterableDataProvider) {
        checkNotNull(itemClass);
        checkNotNull(hasFilterableDataProvider);

        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasFilterableDataProvider.getDataProvider();

        checkNotNull(dataProvider);

        hasFilterableDataProvider.setDataProvider(
                cacheBuilderSpec != null
                        ? new CachingAuthorizedDataProvider<>(dataProvider, itemClass, cacheBuilderSpec)
                        : new AuthorizedDataProvider<>(dataProvider, itemClass)
        );

        return this;
    }

    @Override
    public Unbind unbind(Component... components) {
        checkNotNull(components);
        checkArgument(components.length > 0);
        return new UnbindImpl(this, components);
    }

    @Override
    public Unbind unbindView(View... views) {
        checkNotNull(views);
        checkArgument(views.length > 0);
        return new ViewUnbindImpl(this, views);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> boolean unbind(HasFilterableDataProvider<T, F> hasFilterableDataProvider) {
        checkNotNull(hasFilterableDataProvider);

        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasFilterableDataProvider.getDataProvider();

        if (dataProvider instanceof AuthorizedDataProvider) {
            checkState(boundHasFilteredDataProviders.remove(hasFilterableDataProvider));
            final DataProvider unwrappedDataProvider = ((AuthorizedDataProvider) dataProvider).dataProvider;
            hasFilterableDataProvider.setDataProvider(unwrappedDataProvider);
            return true;
        } else {
            return boundHasFilteredDataProviders.remove(hasFilterableDataProvider);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> boolean unbind(HasDataProvider<T> hasDataProvider) {
        checkNotNull(hasDataProvider);

        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasDataProvider.getDataProvider();

        if (dataProvider instanceof AuthorizedDataProvider) {
            final DataProvider unwrappedDataProvider = ((AuthorizedDataProvider) dataProvider).dataProvider;
            hasDataProvider.setDataProvider(unwrappedDataProvider);

            checkState(boundHasDataProviders.remove(hasDataProvider));
            return true;
        } else {
            return boundHasDataProviders.remove(hasDataProvider);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean evaluate(Collection<Object> permissions) {
        for (Object permission : permissions) {
            Evaluator evaluator = evaluatorPool.getEvaluator(permission.getClass());
            if (!evaluator.evaluate(permission)) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean evaluate(Object permission) {
        checkNotNull(permission, "permission cannot be null");

        final Evaluator evaluator = evaluatorPool.getEvaluator(permission.getClass());
        return evaluator.evaluate(permission);
    }

    @SuppressWarnings("unchecked")
    private boolean evaluate(Collection<Object> permissions, Object2BooleanMap<Object> grantCache) {
        if (grantCache == null) {
            return evaluate(permissions);
        }

        for (Object permission : permissions) {
            boolean granted;

            if (grantCache.containsKey(permission)) {
                granted = grantCache.getBoolean(permission);
            } else {
                final Evaluator evaluator = evaluatorPool.getEvaluator(permission.getClass());
                granted = evaluator.evaluate(permission);
                grantCache.put(permission, granted);
            }

            if (!granted) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void applyAll() {
        Object2BooleanMap<Object> grantCache = new Object2BooleanOpenHashMap<>(componentsToPermissions.values().size());

        synchronized (this) {
            for (Map.Entry<Component, Collection<Object>> entry : componentsToPermissions.asMap().entrySet()) {
                final Collection<Object> permissions = entry.getValue();
                final Component component = entry.getKey();

                if (!allowManualSettingOfVisibility && componentsToLastKnownVisibilityState.containsKey(component)) {
                    checkState(
                            componentsToLastKnownVisibilityState.getBoolean(component) == component.isVisible(),
                            "Component.setVisible() must not be called for components in the vaadin-authorization context, " +
                                    "consider making these components invisible via CSS instead if you want to hide them. In Component %s",
                            component
                    );
                }

                final boolean newVisibility = evaluate(permissions, grantCache);
                component.setVisible(newVisibility);

                if (!allowManualSettingOfVisibility) {
                    componentsToLastKnownVisibilityState.put(component, newVisibility);
                }
            }

            reEvaluateCurrentViewAccess();
        }
    }

    @Override
    public void apply(Component... components) {
        checkNotNull(components);

        Object2BooleanMap<Object> grantCache = components.length > 1
                ? new Object2BooleanOpenHashMap<>(componentsToPermissions.values().size())
                : null;

        synchronized (this) {
            for (Component component : components) {
                if (!allowManualSettingOfVisibility && componentsToLastKnownVisibilityState.containsKey(component)) {
                    checkState(
                            componentsToLastKnownVisibilityState.getBoolean(component) == component.isVisible(),
                            "Component.setVisible() must not be called for components in the vaadin-authorization context, " +
                                    "consider making these components invisible via CSS instead if you want to hide them. In Component %s",
                            component
                    );
                }

                final Collection<Object> permissions = componentsToPermissions.get(component);

                boolean newVisibility = evaluate(permissions, grantCache);

                if (!allowManualSettingOfVisibility) {
                    componentsToLastKnownVisibilityState.put(component, newVisibility);
                }

                component.setVisible(newVisibility);
            }

            reEvaluateCurrentViewAccess();
        }
    }

    Navigator getNavigator() {
        final UI ui = UI.getCurrent();

        return ui == null ? null : ui.getNavigator();
    }

    private void reEvaluateCurrentViewAccess() {
        final Navigator navigator = getNavigator();

        if (navigator == null) {
            //no navigator -> no views to check
            return;
        }

        navigator.navigateTo(navigator.getState());
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        final View newView = event.getNewView();

        final Collection<Object> neededPermissions = Optional
                .ofNullable(viewsToPermissions.get(newView))
                .orElse(ImmutableList.of());

        return evaluate(neededPermissions);
    }

    @Override
    public void afterViewChange(ViewChangeEvent event) {
    }

    private class AuthorizedDataProvider<T, F> implements DataProvider<T, F> {
        final DataProvider<T, F> dataProvider;
        private final Class<T> itemClass;

        AuthorizedDataProvider(DataProvider<T, F> dataProvider, Class<T> itemClass) {
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
            final Evaluator<T> evaluator = evaluatorPool.getEvaluator(itemClass);

            return dataProvider
                    .fetch(query)
                    .filter(evaluator::evaluate);
        }

        @Override
        public void refreshItem(T item) {
            final Evaluator<T> evaluator = evaluatorPool.getEvaluator(itemClass);

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

    private class CachingAuthorizedDataProvider<T, F> extends AuthorizedDataProvider<T, F> {

        private final Cache<Query<T, F>, List<T>> streamCache;

        CachingAuthorizedDataProvider(DataProvider<T, F> dataProvider, Class<T> itemClass, CacheBuilderSpec cacheBuilderSpec) {
            super(dataProvider, itemClass);

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
}
