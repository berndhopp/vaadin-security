package org.vaadin.security.impl;

import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasFilterableDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import org.vaadin.security.api.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.ImmutableSet.copyOf;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("unused")
public class AuthorizationEngine implements Binder, Applier, ViewGuard {

    private static boolean setUp = false;
    final Multimap<Component, Object> componentsToPermissions = HashMultimap.create();
    final Multimap<View, Object> viewsToPermissions = HashMultimap.create();
    final EvaluatorPool evaluatorPool;
    private final CacheBuilderSpec cacheBuilderSpec;
    private final Set<HasDataProvider<?>> boundHasDataProviders = new HashSet<>();
    private final Set<HasFilterableDataProvider<?, ?>> boundHasFilteredDataProviders = new HashSet<>();
    private final Map<Component, Boolean> componentsToLastKnownVisibilityState;
    private final boolean allowManualSettingOfVisibility;

    AuthorizationEngine(EvaluatorPool evaluatorPool, boolean allowManualSettingOfVisibility, CacheBuilderSpec cacheBuilderSpec) {
        this.allowManualSettingOfVisibility = allowManualSettingOfVisibility;
        this.evaluatorPool = checkNotNull(evaluatorPool);

        componentsToLastKnownVisibilityState = allowManualSettingOfVisibility
                ? null
                : new HashMap<>();

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
                        ? new CachingAuthorizedDataProvider<>(this, dataProvider, itemClass, cacheBuilderSpec)
                        : new AuthorizedDataProvider<>(this, dataProvider, itemClass)
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
                        ? new CachingAuthorizedDataProvider<>(this, dataProvider, itemClass, cacheBuilderSpec)
                        : new AuthorizedDataProvider<>(this, dataProvider, itemClass)
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
    private boolean evaluate(Object permission) {
        final Evaluator evaluator = evaluatorPool.getEvaluator(permission.getClass());
        return evaluator.evaluate(permission);
    }

    @Override
    public void applyAll() {
        applyInternal(componentsToPermissions.asMap());
    }

    @Override
    public void apply(Component... components) {
        checkNotNull(components);
        applyInternal(stream(components).collect(toMap(c -> c, componentsToPermissions::get)));
    }

    void applyInternal(Map<Component, Collection<Object>> componentsToPermissions) {
        synchronized (this) {
            Stream<Object> distinctPermissionsStream = componentsToPermissions
                    .values()
                    .stream() //streaming all bound permissions
                    .flatMap(Collection::stream) //unrolling them from the collections
                    .distinct();

            /* TODO too many parameters, builder maybe?
            if(parallelPermissionEvaluation){
                //now we can go multithreaded
                distinctPermissionsStream = distinctPermissionsStream.parallel();
            }
            */

            //mapping all permissions to their evaluation
            final Map<Object, Boolean> permissionsToEvaluations = distinctPermissionsStream.collect(toMap(p -> p, this::evaluate));

            for (Map.Entry<Component, Collection<Object>> entry : componentsToPermissions.entrySet()) {
                final Collection<Object> permissions = entry.getValue();
                final Component component = entry.getKey();

                if (!allowManualSettingOfVisibility && componentsToLastKnownVisibilityState.containsKey(component)) {
                    checkState(
                            componentsToLastKnownVisibilityState.get(component) == component.isVisible(),
                            "Component.setVisible() must not be called for components in the vaadin-authorization context, " +
                                    "consider making these components invisible via CSS instead if you want to hide them. In Component %s",
                            component
                    );
                }

                final boolean newVisibility = permissions.stream().allMatch(permissionsToEvaluations::get);

                component.setVisible(newVisibility);

                if (!allowManualSettingOfVisibility) {
                    componentsToLastKnownVisibilityState.put(component, newVisibility);
                }
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

        final String state = navigator.getState();
        navigator.navigateTo("");
        navigator.navigateTo(state);
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        final View newView = event.getNewView();

        final Collection<Object> permissions = viewsToPermissions.get(newView);

        if (permissions == null) {
            return true;
        }

        final boolean granted = evaluate(permissions);

        if(!granted){
            getNavigator().navigateTo("");
        }

        return granted;
    }
}
