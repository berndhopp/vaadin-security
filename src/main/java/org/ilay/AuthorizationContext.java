package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import org.ilay.api.Authorizer;
import org.ilay.api.Reverter;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.lang.String.format;
import static java.util.Collections.EMPTY_SET;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * the {@link com.vaadin.server.VaadinSession}-bound AuthorizationContext contains all bindings of
 * {@link View}s and {@link Component}s to permissions, the set of {@link Authorizer}s and some more
 * contextual data.
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
class AuthorizationContext implements ViewChangeListener {

    private final Map<Component, Set<Object>> componentsToPermissions = new WeakHashMap<>();
    private final Map<View, Set<Object>> viewsToPermissions = new WeakHashMap<>();
    private final AuthorizerPool authorizerPool;
    private final Map<Component, Boolean> trackedVisibilities = new WeakHashMap<>();
    private final Set<Reference<DataProvider>> dataProviders = new HashSet<>();
    private boolean registeredAsViewChangeListener = false;

    private AuthorizationContext(Set<Authorizer> authorizers) {
        this.authorizerPool = new AuthorizerPool(authorizers);
    }

    static void initSession(Set<Authorizer> authorizers) {
        requireNonNull(authorizers);
        final AuthorizationContext authorizationContext = new AuthorizationContext(authorizers);
        VaadinAbstraction.storeInSession(AuthorizationContext.class, authorizationContext);
    }

    static AuthorizationContext getCurrent() {
        final Optional<AuthorizationContext> authorizationContext = VaadinAbstraction.getFromSessionStore(AuthorizationContext.class);

        return Check.present(authorizationContext);
    }

    Map<Component, Set<Object>> getComponentsToPermissions() {
        return componentsToPermissions;
    }

    Map<View, Set<Object>> getViewsToPermissions() {
        return viewsToPermissions;
    }

    @SuppressWarnings("unchecked")
    <T, F> Reverter bindData(Class<T> itemClass, VaadinAbstraction.DataProviderHolder dataProviderHolder) {
        requireNonNull(itemClass);
        requireNonNull(dataProviderHolder);

        final Authorizer<T, F> authorizer = authorizerPool.getAuthorizer(itemClass);
        final DataProvider<T, F> oldDataProvider = (DataProvider<T, F>) dataProviderHolder.getDataProvider();
        final DataProvider<T, F> newDataProvider = new AuthorizingDataProvider<>(oldDataProvider, authorizer);

        dataProviderHolder.setDataProvider(newDataProvider);

        dataProviders.add(new WeakReference<>(newDataProvider));

        return new DataReverter(new WeakReference<>(dataProviderHolder));
    }

    @SuppressWarnings("unchecked")
    <T, U> void unbindData(VaadinAbstraction.DataProviderHolder dataProviderHolder) {
        requireNonNull(dataProviderHolder);

        final DataProvider<T, U> dataProvider = dataProviderHolder.getDataProvider();

        if (dataProvider instanceof AuthorizingDataProvider) {
            AuthorizingDataProvider<T, ?, ?> authorizingDataProvider = (AuthorizingDataProvider<T, ?, ?>) dataProvider;

            final DataProvider<T, ?> wrappedDataProvider = authorizingDataProvider.getWrappedDataProvider();

            dataProviderHolder.setDataProvider(wrappedDataProvider);

            Check.state(dataProviders.removeIf(r -> r.get() == dataProvider));
        }
    }

    void applyComponents(Map<Component, Set<Object>> componentsToPermissions) throws IllegalStateException {

        //this is the cache for all current evaluation-values ( granted / not granted )
        final Map<Object, Boolean> permissionsToEvaluations = componentsToPermissions
                .values()
                .stream() //streaming all bound permissions
                .flatMap(Collection::stream) //unrolling them from the collections
                .distinct() // have each permission only once
                .collect(toMap(p -> p, this::isGranted));//mapping all permissions to their evaluation

        for (Map.Entry<Component, Set<Object>> entry : componentsToPermissions.entrySet()) {
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
                .filter(Objects::nonNull)
                .forEach(DataProvider::refreshAll);
    }

    @SuppressWarnings("unchecked")
    boolean isGranted(Object permission) {
        requireNonNull(permission);
        final Authorizer authorizer = authorizerPool.getAuthorizer(permission.getClass());
        return authorizer.isGranted(permission);
    }

    void addPermissions(Component component, Set<Object> permissions) {
        requireNonNull(component);
        requireNonNull(permissions);

        Set<Object> currentPermissions = componentsToPermissions.get(component);

        if (currentPermissions == null) {
            Set<Object> newPermissions = new CopyOnWriteArraySet<>(permissions);
            componentsToPermissions.put(component, newPermissions);
        } else {
            currentPermissions.addAll(permissions);
        }
    }

    void addPermissions(View view, Set<Object> permissions) {
        requireNonNull(view);
        requireNonNull(permissions);

        final Set<Object> currentPermissions = viewsToPermissions.get(view);

        if (currentPermissions == null) {
            Set<Object> newPermissions = new CopyOnWriteArraySet<>(permissions);
            viewsToPermissions.put(view, newPermissions);
        } else {
            currentPermissions.addAll(permissions);
        }
    }

    void removePermissions(Component component, Set<Object> permissions) {
        requireNonNull(component);
        requireNonNull(permissions);

        Set<Object> currentPermissions = componentsToPermissions.get(component);

        if (currentPermissions != null) {
            currentPermissions.removeAll(permissions);
        }
    }

    void removePermissions(View view, Set<Object> permissions) {
        requireNonNull(view);
        requireNonNull(permissions);

        final Set<Object> currentPermissions = viewsToPermissions.get(view);

        if (currentPermissions != null) {
            currentPermissions.removeAll(permissions);
        }
    }

    void ensureViewChangeListenerRegistered() {

        if (registeredAsViewChangeListener) {
            return;
        }

        final Optional<VaadinAbstraction.NavigatorFacade> navigator = VaadinAbstraction.getNavigatorFacade();

        Check.state(navigator.isPresent(), "a navigator needs to be registered on the current UI before this method can be called");

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        final VaadinAbstraction.NavigatorFacade navigatorFacade = navigator.get();

        navigatorFacade.addViewChangeListener(this);

        registeredAsViewChangeListener = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean beforeViewChange(ViewChangeEvent event) {
        requireNonNull(event);

        final View newView = requireNonNull(event.getNewView());

        final Collection<Object> requiredPermissions = getRequiredPermissions(newView);

        if (!requiredPermissions.stream().allMatch(this::isGranted)) {
            return false;
        }

        if (newView instanceof TypedAuthorizationView) {
            Check.notNullOrEmpty(event.getParameters());

            TypedAuthorizationView typedAuthorizationView = (TypedAuthorizationView) newView;

            Object parsed;

            try {
                parsed = typedAuthorizationView.parse(event.getParameters());
            } catch (TypedAuthorizationView.ParseException e) {
                return false;
            }

            requireNonNull(parsed, () -> format("%s#parse() must not return null", newView.getClass()));

            if (isGranted(parsed)) {
                typedAuthorizationView.enter(parsed);
            } else {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private Set<Object> getRequiredPermissions(View view) {
        return Optional.ofNullable(viewsToPermissions.get(view))
                .orElse((Set<Object>) EMPTY_SET);
    }
}
