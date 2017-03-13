package org.vaadin.security;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasFilterableDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import org.vaadin.security.api.Applier;
import org.vaadin.security.api.Binder;
import org.vaadin.security.api.Evaluator;
import org.vaadin.security.api.EvaluatorPool;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.copyOf;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("unused")
public class AuthorizationEngine implements Binder, Applier {

    private static boolean setUp = false;
    final Multimap<Component, Object> componentsToPermissions = HashMultimap.create();
    final Multimap<View, Object> viewsToPermissions = HashMultimap.create();
    private final EvaluatorPool evaluatorPool;
    private final Map<Component, Boolean> componentsToLastKnownVisibilityState;
    private final Set<DataProvider<?, ?>> dataProviders = new HashSet<>();
    private final boolean allowManualSettingOfVisibility;

    AuthorizationEngine(EvaluatorPool evaluatorPool, boolean allowManualSettingOfVisibility) {
        this.allowManualSettingOfVisibility = allowManualSettingOfVisibility;
        this.evaluatorPool = checkNotNull(evaluatorPool);

        componentsToLastKnownVisibilityState = allowManualSettingOfVisibility
                ? null
                : new HashMap<>();
    }

    public static void start(Supplier<EvaluatorPool> evaluatorPoolSupplier) {
        start(evaluatorPoolSupplier, false);
    }

    public static void start(Supplier<EvaluatorPool> evaluatorPoolSupplier, boolean allowManualSettingOfVisibility) {

        checkState(!setUp, "setUp() cannot be called more than once");
        checkNotNull(evaluatorPoolSupplier);

        final VaadinService vaadinService = VaadinService.getCurrent();

        checkState(vaadinService != null, "VaadinService is not initialized yet");

        vaadinService.addSessionInitListener(
                event -> {
                    final EvaluatorPool evaluatorPool = checkNotNull(evaluatorPoolSupplier.get());

                    AuthorizationEngine authorizationEngine = new AuthorizationEngine(evaluatorPool, allowManualSettingOfVisibility);

                    final VaadinSession session = event.getSession();

                    session.setAttribute(Binder.class, authorizationEngine);
                    session.setAttribute(Applier.class, authorizationEngine);
                    session.setAttribute(AuthorizationEngine.class, authorizationEngine);
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
    public Bind bindComponents(Component... components) {
        checkNotNull(components);
        checkArgument(components.length > 0);
        return new BindImpl(this, components);
    }

    @Override
    public Bind bindViews(View... views) {
        checkNotNull(views);
        checkArgument(views.length > 0);
        return new ViewBindImpl(this, views);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> Binder bindHasDataProvider(HasDataProvider<T> hasDataProvider) {
        checkNotNull(hasDataProvider);


        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasDataProvider.getDataProvider();

        checkNotNull(dataProvider);

        checkArgument(
                dataProvider instanceof ListDataProvider,
                "thus far, we can only handle ListDataProvider, sorry"
        );

        ListDataProvider<T> listDataProvider = (ListDataProvider<T>) dataProvider;

        dataProviders.add(dataProvider);

        listDataProvider.addFilter(new EvaluatorPredicate<>(this));

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> Binder bindHasDataProvider(HasFilterableDataProvider<T, F> hasFilterableDataProvider) {
        checkNotNull(hasFilterableDataProvider);

        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasFilterableDataProvider.getDataProvider();

        checkNotNull(dataProvider);

        checkArgument(
                dataProvider instanceof ListDataProvider,
                "thus far, we can only handle ListDataProvider, sorry"
        );

        ListDataProvider<T> listDataProvider = (ListDataProvider<T>) dataProvider;

        dataProviders.add(dataProvider);

        listDataProvider.addFilter(new EvaluatorPredicate<>(this));

        return this;
    }

    @Override
    public Unbind unbindComponents(Component... components) {
        checkNotNull(components);
        checkArgument(components.length > 0);
        return new UnbindImpl(this, components);
    }

    @Override
    public Unbind unbindViews(View... views) {
        checkNotNull(views);
        checkArgument(views.length > 0);
        return new ViewUnbindImpl(this, views);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> boolean unbindHasDataProvider(HasFilterableDataProvider<T, F> hasFilterableDataProvider) {
        checkNotNull(hasFilterableDataProvider);

        ListDataProvider<T> listDataProvider = (ListDataProvider<T>) hasFilterableDataProvider.getDataProvider();

        throw new RuntimeException("not implemented yet");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> boolean unbindHasDataProvider(HasDataProvider<T> hasDataProvider) {
        checkNotNull(hasDataProvider);
        throw new RuntimeException("not implemented yet");
    }

    @SuppressWarnings("unchecked")
    boolean evaluate(Object permission) {
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

    private void applyInternal(Map<Component, Collection<Object>> componentsToPermissions) {
        //TODO consider parallelization
        final Map<Object, Boolean> permissionsToEvaluations = componentsToPermissions
                .values()
                .stream() //streaming all bound permissions
                .flatMap(Collection::stream) //unrolling them from the collections
                .distinct() // have each permission only once
                .collect(toMap(p -> p, this::evaluate));//mapping all permissions to their evaluation

        for (Map.Entry<Component, Collection<Object>> entry : componentsToPermissions.entrySet()) {
            final Collection<Object> permissions = entry.getValue();
            final Component component = entry.getKey();

            final boolean newVisibility = permissions.stream().allMatch(permissionsToEvaluations::get);

            if (!allowManualSettingOfVisibility) {
                final Boolean lastVisibilityState = componentsToLastKnownVisibilityState.get(component);

                checkState(
                        lastVisibilityState == null || lastVisibilityState == component.isVisible(),
                        "Component.setVisible() must not be called for components in the vaadin-authorization context, " +
                                "consider making these components invisible via CSS instead if you want to hide them. In Component %s",
                        component
                );

                componentsToLastKnownVisibilityState.put(component, newVisibility);
            }

            component.setVisible(newVisibility);
        }

        reEvaluateCurrentViewAccess();

        dataProviders.forEach(DataProvider::refreshAll);
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

    public boolean navigationAllowed(View newView) {
        checkNotNull(newView);

        for (Object permission : viewsToPermissions.get(newView)) {
            if (!evaluate(permission)) {
                return false;
            }
        }

        return true;
    }

}
