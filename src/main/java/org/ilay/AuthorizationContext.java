package org.ilay;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import org.ilay.api.Authorizer;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

class AuthorizationContext implements ViewChangeListener {

    static TestSupport.Vessel<AuthorizationContext> currentInstanceVessel = new TestSupport.ProductionAuthorizationContextVessel();
    private final Map<Component, Set<Object>> componentsToPermissions = new WeakHashMap<>();
    private final Map<View, Set<Object>> viewsToPermissions = new WeakHashMap<>();
    private final AuthorizerPool authorizerPool;
    private final Map<Component, Boolean> trackedVisibilities = new WeakHashMap<>();
    private final Set<Reference<DataProvider<?, ?>>> dataProviders = new HashSet<>();
    private boolean registeredAsViewChangeListener = false;

    private AuthorizationContext(Set<Authorizer> authorizers) {
        this.authorizerPool = new AuthorizerPool(authorizers);
    }

    static void init(Set<Authorizer> authorizers) {
        requireNonNull(authorizers);
        currentInstanceVessel.set(new AuthorizationContext(authorizers));
    }

    static AuthorizationContext getCurrent() {
        return currentInstanceVessel.get();
    }

    Map<Component, Set<Object>> getComponentsToPermissions() {
        return componentsToPermissions;
    }

    Map<View, Set<Object>> getViewsToPermissions() {
        return viewsToPermissions;
    }

    @SuppressWarnings("unchecked")
    <T, F> void bindData(Class<T> itemClass, HasDataProvider<T> hasDataProvider) {
        requireNonNull(itemClass);
        requireNonNull(hasDataProvider);

        final Authorizer<T, F> authorizer = authorizerPool.getAuthorizer(itemClass);
        final DataProvider<T, F> oldDataProvider = (DataProvider<T, F>) hasDataProvider.getDataProvider();
        final DataProvider<T, F> newDataProvider = new Authorization.AuthorizingDataProvider<>(oldDataProvider, authorizer);

        hasDataProvider.setDataProvider(newDataProvider);

        dataProviders.add(new WeakReference<>(newDataProvider));
    }

    <T> boolean unbindData(HasDataProvider<T> hasDataProvider) {
        requireNonNull(hasDataProvider);

        final DataProvider<T, ?> dataProvider = hasDataProvider.getDataProvider();

        if (dataProvider == null) {
            return false;
        }

        if (dataProvider instanceof Authorization.AuthorizingDataProvider) {
            Authorization.AuthorizingDataProvider<T, ?, ?> authorizingDataProvider = (Authorization.AuthorizingDataProvider<T, ?, ?>) dataProvider;

            final DataProvider<T, ?> wrappedDataProvider = authorizingDataProvider.getWrappedDataProvider();

            hasDataProvider.setDataProvider(wrappedDataProvider);

            return true;
        }

        return false;
    }

    void applyComponents(Map<Component, Set<Object>> componentsToPermissions) throws IllegalStateException {

        //this is the cache for all current evaluation-values ( granted / not granted )
        final Map<Object, Boolean> permissionsToEvaluations = componentsToPermissions
                .values()
                .stream() //streaming all bound permissions
                .flatMap(Collection::stream) //unrolling them from the collections
                .distinct() // have each permission only once
                .collect(toMap(p -> p, this::evaluate));//mapping all permissions to their evaluation

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
                .filter(o -> o != null)
                .forEach(DataProvider::refreshAll);
    }

    @SuppressWarnings("unchecked")
    boolean evaluate(Object permission) {
        final Authorizer authorizer = authorizerPool.getAuthorizer(permission.getClass());
        return authorizer.isGranted(permission);
    }

    void ensureViewChangeListenerRegistered() {

        if (registeredAsViewChangeListener) {
            return;
        }

        final TestSupport.NavigatorFacade navigator = Authorization.navigatorSupplier.get();

        navigator.addViewChangeListener(this);

        registeredAsViewChangeListener = true;
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        requireNonNull(event);

        final View newView = requireNonNull(event.getNewView());

        return isViewAuthorized(newView);
    }

    boolean isViewAuthorized(View newView) {
        final Collection<Object> permissions = viewsToPermissions.get(newView);

        return permissions == null || permissions.stream().allMatch(this::evaluate);
    }

    static class AuthorizerPool {

        private final Map<Class<?>, Authorizer<?, ?>> authorizers;

        AuthorizerPool(Collection<Authorizer> authorizers) {
            requireNonNull(authorizers);
            this.authorizers = new HashMap<>(authorizers.size());

            for (Authorizer authorizer : authorizers) {
                requireNonNull(authorizer);
                requireNonNull(authorizer.getPermissionClass());

                Authorizer<?, ?> alreadyRegistered = this.authorizers.put(authorizer.getPermissionClass(), authorizer);

                if (alreadyRegistered != null) {
                    throw new ConflictingEvaluatorsException(authorizer, alreadyRegistered, authorizer.getPermissionClass());
                }
            }
        }

        @SuppressWarnings("unchecked")
        <T, F> Authorizer<T, F> getAuthorizer(Class<T> permissionClass) {

            requireNonNull(permissionClass);

            Authorizer<T, F> authorizer = (Authorizer<T, F>) authorizers.get(permissionClass);

            if (authorizer != null) {
                return authorizer;
            }

            for (Authorizer<?, ?> anAuthorizer : authorizers.values()) {

                //TODO this needs explanation, as soon as I can wrap my own head around it
                boolean match = permissionClass.isInterface()
                        ? permissionClass.isAssignableFrom(anAuthorizer.getPermissionClass())
                        : anAuthorizer.getPermissionClass().isAssignableFrom(permissionClass);

                if (match) {
                    if (authorizer != null) {
                        throw new ConflictingEvaluatorsException(authorizer, anAuthorizer, permissionClass);
                    }

                    authorizer = (Authorizer<T, F>) anAuthorizer;
                }
            }

            Check.arg(authorizer != null, "no authorizer found for %s", permissionClass);

            authorizers.put(permissionClass, authorizer);

            return authorizer;
        }

        static class ConflictingEvaluatorsException extends RuntimeException {

            ConflictingEvaluatorsException(Authorizer authorizer1, Authorizer authorizer2, Class permissionClass) {
                super(
                        format(
                                "conflicting navigators: %s and %s are both assignable to %s",
                                authorizer1,
                                authorizer2,
                                permissionClass)
                );
            }
        }
    }
}
