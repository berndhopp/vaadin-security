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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.lang.String.format;
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

        final Authorizer<T> authorizer = authorizerPool.getAuthorizer(itemClass);

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
    private boolean isGranted(Object permission) {
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

        final Optional<VaadinAbstraction.Navigator> navigatorOptional = VaadinAbstraction.getNavigator();

        Check.state(navigatorOptional.isPresent(), "a navigator needs to be registered on the current UI before Authorization.bindView() or Authorization.bindViews() can be called");

        @SuppressWarnings("OptionalGetWithoutIsPresent") final VaadinAbstraction.Navigator navigatorFacade = navigatorOptional.get();

        navigatorFacade.addViewChangeListener(this);

        registeredAsViewChangeListener = true;
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        requireNonNull(event);
        final boolean navigationAllowed = navigationAllowed(event.getNewView(), event.getParameters());

        if (!navigationAllowed && event.getNewView().equals(event.getOldView())) {
            //in case the user does not have access to the view he is currently on, go to default view
            //noinspection ConstantConditions
            VaadinAbstraction.getNavigator().get().navigateTo("");
        }

        return navigationAllowed;
    }

    @SuppressWarnings("unchecked")
    private <T> boolean navigationAllowed(View newView, String parameters) {
        requireNonNull(newView);

        final Set<Object> permissions = viewsToPermissions.get(newView);

        if (permissions != null) {
            for (Object permission : permissions) {
                if (!isGranted(permission)) {
                    return false;
                }
            }
        }

        if (newView instanceof TypedAuthorizationView) {
            Check.notNullOrEmpty(parameters);

            TypedAuthorizationView<T> typedAuthorizationView = (TypedAuthorizationView<T>) newView;

            T t;

            try {
                t = requireNonNull(
                        typedAuthorizationView.parse(parameters),
                        () -> format("%s#parse() must not return null", newView.getClass())
                );
            } catch (TypedAuthorizationView.ParseException e) {
                return false;
            }

            if (!isGranted(t)) {
                return false;
            }

            typedAuthorizationView.enter(t);
        }

        return true;
    }
}
