package org.vaadin.authorization;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasFilterableDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;

import org.vaadin.authorization.Authorization.Evaluator;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

class AuthorizationContext {

    static Supplier<AuthorizationContext> instanceProvider = () -> {
        final VaadinSession vaadinSession = VaadinSession.getCurrent();

        requireNonNull(vaadinSession, "no VaadinSession available");

        final AuthorizationContext authorizationContext = vaadinSession.getAttribute(AuthorizationContext.class);

        return requireNonNull(
                authorizationContext,
                "no authorizationContext available in the current session, did you forget" +
                        "to call Authorization.start()?"
        );
    };
    private final Map<Component, Collection<Object>> componentsToPermissions = new WeakHashMap<>();
    private final Map<View, Collection<Object>> viewsToPermissions = new WeakHashMap<>();
    private final EvaluatorPool evaluatorPool;
    private final Map<Component, Boolean> trackedVisibilities = new WeakHashMap<>();
    private final Set<Reference<DataProvider<?, ?>>> dataProviders = new HashSet<>();

    private AuthorizationContext(Set<Evaluator> evaluators) {
        this.evaluatorPool = new EvaluatorPool(evaluators);
    }

    static void init(Set<Evaluator> evaluators) {
        requireNonNull(evaluators);
        AuthorizationContext authorizationContext = new AuthorizationContext(evaluators);
        final VaadinSession vaadinSession = VaadinSession.getCurrent();

        requireNonNull(vaadinSession, "no VaadinSession available");

        vaadinSession.setAttribute(AuthorizationContext.class, authorizationContext);
    }

    static AuthorizationContext getCurrent() {
        return instanceProvider.get();
    }

    Map<Component, Collection<Object>> getComponentsToPermissions() {
        return componentsToPermissions;
    }

    Map<View, Collection<Object>> getViewsToPermissions() {
        return viewsToPermissions;
    }

    Set<Reference<DataProvider<?, ?>>> getDataProviders() {
        return dataProviders;
    }

    @SuppressWarnings("unchecked")
    <T, F> void bindHasDataProvider(HasDataProvider<T> hasDataProvider) {
        requireNonNull(hasDataProvider);

        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasDataProvider.getDataProvider();

        requireNonNull(dataProvider);

        if (dataProvider instanceof ListDataProvider) {
            ListDataProvider<T> listDataProvider = (ListDataProvider<T>) dataProvider;
            AuthorizationContext.getCurrent().getDataProviders().add(new WeakReference<>(dataProvider));
            listDataProvider.addFilter(new EvaluatorPredicate<>());
        } else {
            hasDataProvider.setDataProvider(new DataProviderWrapper<>(this, dataProvider));
        }
    }

    @SuppressWarnings("unchecked")
    <T, F> void bindHasDataProvider(HasFilterableDataProvider<T, F> hasFilterableDataProvider) {
        requireNonNull(hasFilterableDataProvider);

        final DataProvider<T, F> dataProvider = (DataProvider<T, F>) hasFilterableDataProvider.getDataProvider();

        requireNonNull(dataProvider);

        if (dataProvider instanceof ListDataProvider) {
            ListDataProvider<T> listDataProvider = (ListDataProvider<T>) dataProvider;
            AuthorizationContext.getCurrent().getDataProviders().add(new WeakReference<>(dataProvider));
            listDataProvider.addFilter(new EvaluatorPredicate<>());
        } else {
            hasFilterableDataProvider.setDataProvider(new DataProviderWrapper<>(this, dataProvider));
        }
    }

    void applyComponents(Map<Component, Collection<Object>> componentsToPermissions) throws IllegalStateException {

        //this is the cache for all current evaluation-values ( granted / not granted )
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

            final Boolean trackedVisibility = trackedVisibilities.get(component);

            if (trackedVisibility != null && trackedVisibility != component.isVisible()) {
                throw new IllegalStateException(
                        format(
                                "Component.setVisible() must not be called for components in the vaadin-authorization context, " +
                                        "consider making these components invisible via CSS instead if you want to hide them. In Component %s",
                                component
                        )
                );
            }

            trackedVisibilities.put(component, newVisibility);
            component.setVisible(newVisibility);
        }
    }

    void applyData() {
        dataProviders
                .stream()
                .map(Reference::get)
                .filter(o -> o != null)
                .forEach(DataProvider::refreshAll);
    }

    @SuppressWarnings("unchecked")
    boolean evaluate(Object permission) {
        final Evaluator evaluator = evaluatorPool.getEvaluator(permission.getClass());
        return evaluator.evaluate(permission);
    }

    static class EvaluatorPredicate<T> implements SerializablePredicate<T> {

        EvaluatorPredicate() {
        }

        @Override
        public boolean test(T permission) {
            final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
            return authorizationContext.evaluate(permission);
        }
    }

}
