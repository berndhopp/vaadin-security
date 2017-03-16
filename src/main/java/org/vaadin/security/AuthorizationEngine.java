package org.vaadin.security;

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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("unused")
public class AuthorizationEngine implements Binder, Applier {

    private static boolean setUp = false;
    final Map<Component, Collection<Object>> componentsToPermissions = new WeakHashMap<>();
    final Map<View, Collection<Object>> viewsToPermissions = new WeakHashMap<>();
    private final EvaluatorPool evaluatorPool;
    private final Map<Component, Boolean> componentsToLastKnownVisibilityState = new WeakHashMap<>();
    private final Set<Reference<DataProvider<?, ?>>> dataProviders = new HashSet<>();

    AuthorizationEngine(EvaluatorPool evaluatorPool) {
        this.evaluatorPool = requireNonNull(evaluatorPool);
    }

    public static void start(Supplier<EvaluatorPool> evaluatorPoolSupplier) {

        if (setUp) {
            throw new IllegalStateException("setUp() cannot be called more than once");
        }
        requireNonNull(evaluatorPoolSupplier);

        final VaadinService vaadinService = VaadinService.getCurrent();

        if (vaadinService == null) {
            throw new IllegalStateException("VaadinService is not initialized yet");
        }

        vaadinService.addSessionInitListener(
                event -> {
                    final EvaluatorPool evaluatorPool = requireNonNull(evaluatorPoolSupplier.get());

                    AuthorizationEngine authorizationEngine = new AuthorizationEngine(evaluatorPool);

                    final VaadinSession session = event.getSession();

                    session.setAttribute(Binder.class, authorizationEngine);
                    session.setAttribute(Applier.class, authorizationEngine);
                    session.setAttribute(AuthorizationEngine.class, authorizationEngine);
                }
        );

        setUp = true;
    }

    @Override
    public Collection<Object> getPermissions(Component component) {
        requireNonNull(component);
        final Collection<Object> permissions = componentsToPermissions.get(component);

        if (permissions == null) {
            return emptyList();
        }

        return unmodifiableCollection(permissions);
    }

    @Override
    public Collection<Object> getViewPermissions(View view) {
        requireNonNull(view);
        final Collection<Object> permissions = viewsToPermissions.get(view);

        if (permissions == null) {
            return emptyList();
        }

        return unmodifiableCollection(permissions);
    }

    @Override
    public Bind bindComponents(Component... components) {
        requireNonNull(components);
        if (components.length == 0) {
            throw new IllegalArgumentException("components must not be empty");
        }

        return new BindImpl(this, components);
    }

    @Override
    public Bind bindViews(View... views) {
        requireNonNull(views);
        if (views.length == 0) {
            throw new IllegalArgumentException("components must not be empty");
        }
        return new ViewBindImpl(this, views);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> Binder bindHasDataProvider(HasDataProvider<T> hasDataProvider) {
        requireNonNull(hasDataProvider);


        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasDataProvider.getDataProvider();

        requireNonNull(dataProvider);

        if (!(dataProvider instanceof ListDataProvider)) {
            throw new IllegalArgumentException("thus far, we can only handle ListDataProvider, sorry");
        }

        ListDataProvider<T> listDataProvider = (ListDataProvider<T>) dataProvider;

        dataProviders.add(new WeakReference<>(dataProvider));

        listDataProvider.addFilter(new EvaluatorPredicate<>(this));

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> Binder bindHasDataProvider(HasFilterableDataProvider<T, F> hasFilterableDataProvider) {
        requireNonNull(hasFilterableDataProvider);

        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasFilterableDataProvider.getDataProvider();

        requireNonNull(dataProvider);

        if (!(dataProvider instanceof ListDataProvider)) {
            throw new IllegalArgumentException("thus far, we can only handle ListDataProvider, sorry");
        }

        ListDataProvider<T> listDataProvider = (ListDataProvider<T>) dataProvider;

        dataProviders.add(new WeakReference<>(dataProvider));

        listDataProvider.addFilter(new EvaluatorPredicate<>(this));

        return this;
    }

    @Override
    public Unbind unbindComponents(Component... components) {
        requireNonNull(components);

        if (components.length == 0) {
            throw new IllegalArgumentException("components must not be empty");
        }

        return new UnbindImpl(this, components);
    }

    @Override
    public Unbind unbindViews(View... views) {
        requireNonNull(views);

        if (views.length == 0) {
            throw new IllegalArgumentException("components must not be empty");
        }

        return new ViewUnbindImpl(this, views);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> boolean unbindHasDataProvider(HasFilterableDataProvider<T, F> hasFilterableDataProvider) {
        requireNonNull(hasFilterableDataProvider);

        ListDataProvider<T> listDataProvider = (ListDataProvider<T>) hasFilterableDataProvider.getDataProvider();

        throw new RuntimeException("not implemented yet");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, F> boolean unbindHasDataProvider(HasDataProvider<T> hasDataProvider) {
        requireNonNull(hasDataProvider);
        throw new RuntimeException("not implemented yet");
    }

    @SuppressWarnings("unchecked")
    boolean evaluate(Object permission) {
        final Evaluator evaluator = evaluatorPool.getEvaluator(permission.getClass());
        return evaluator.evaluate(permission);
    }

    @Override
    public void applyAll() {
        applyInternal(componentsToPermissions);
    }

    @Override
    public void apply(Component... components) {
        requireNonNull(components);
        applyInternal(stream(components).collect(toMap(c -> c, componentsToPermissions::get)));
    }

    private void applyInternal(Map<Component, Collection<Object>> componentsToPermissions) throws IllegalStateException {
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

            final Boolean lastVisibilityState = componentsToLastKnownVisibilityState.get(component);

            if (lastVisibilityState != null && lastVisibilityState != component.isVisible()) {
                throw new IllegalStateException(
                        format(
                                "Component.setVisible() must not be called for components in the vaadin-authorization context, " +
                                        "consider making these components invisible via CSS instead if you want to hide them. In Component %s",
                                component
                        )
                );
            }

            componentsToLastKnownVisibilityState.put(component, newVisibility);
            component.setVisible(newVisibility);
        }

        reEvaluateCurrentViewAccess();

        dataProviders
                .stream()
                .map(Reference::get)
                .filter(o -> o != null)
                .forEach(DataProvider::refreshAll);
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
        requireNonNull(newView);

        final Collection<Object> permissions = viewsToPermissions.get(newView);

        return permissions == null || permissions.stream().allMatch(this::evaluate);
    }

}
