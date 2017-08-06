package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import org.ilay.api.Authorizer;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * the {@link com.vaadin.server.VaadinSession}-bound AuthorizationContext contains all bindings of
 * {@link View}s and {@link Component}s to permissions, the set of {@link Authorizer}s and some more
 * contextual data.
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
class AuthorizationContext implements ViewChangeListener, Serializable {

    private static final long serialVersionUID = -4272382552902657102L;

    private final Map<Component, Set<Object>> componentsToPermissions = new WeakHashMap<>();
    private final Map<View, Set<Object>> viewsToPermissions = new WeakHashMap<>();
    private final AuthorizerPool authorizerPool;
    private final Map<Component, Boolean> trackedVisibilities = new WeakHashMap<>();
    private final Set<Reference<DataProvider>> dataProviders = new HashSet<>();
    private String currentParameters = "";
    private boolean registeredAsViewChangeListener = false;

    private AuthorizationContext(Set<Authorizer> authorizers) {
        this.authorizerPool = new AuthorizerPool(authorizers);
    }

    static void initSession(Set<Authorizer> authorizers) {

        requireNonNull(authorizers);
        final AuthorizationContext authorizationContext = new AuthorizationContext(authorizers);
        final VaadinSession vaadinSession = Check.notNull(VaadinSession.getCurrent());

        vaadinSession.setAttribute(AuthorizationContext.class, authorizationContext);
    }

    static AuthorizationContext getCurrent() {
        final VaadinSession vaadinSession = Check.notNull(VaadinSession.getCurrent());
        return Check.notNull(vaadinSession.getAttribute(AuthorizationContext.class));
    }

    Map<Component, Set<Object>> getComponentsToPermissions() {
        return componentsToPermissions;
    }

    Map<View, Set<Object>> getViewsToPermissions() {
        return viewsToPermissions;
    }

    @SuppressWarnings("unchecked")
    <T, F> Registration bindData(Class<T> itemClass, Holder<DataProvider<T, ?>> dataProviderHolder) {
        requireNonNull(itemClass);
        requireNonNull(dataProviderHolder);

        final Authorizer<T> authorizer = authorizerPool.getAuthorizer(itemClass);

        final DataProvider<T, F> oldDataProvider = (DataProvider<T, F>) dataProviderHolder.get();
        final DataProvider<T, F> newDataProvider = new AuthorizingDataProvider<>(oldDataProvider, authorizer);

        dataProviderHolder.set(newDataProvider);

        dataProviders.add(new WeakReference<>(newDataProvider));

        return new DataRegistration(dataProviderHolder);
    }

    @SuppressWarnings("unchecked")
    <T> void unbindData(Holder<DataProvider<T, ?>> dataProviderHolder) {
        requireNonNull(dataProviderHolder);

        final DataProvider<T, ?> dataProvider = dataProviderHolder.get();

        if (dataProvider instanceof AuthorizingDataProvider) {
            AuthorizingDataProvider<T, ?, ?> authorizingDataProvider = (AuthorizingDataProvider<T, ?, ?>) dataProvider;

            final DataProvider<T, ?> wrappedDataProvider = authorizingDataProvider.getWrappedDataProvider();

            dataProviderHolder.set(wrappedDataProvider);

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

    void addPermissions(Component component, Set<Object> newPermissions) {
        requireNonNull(component);
        requireNonNull(newPermissions);

        Set<Object> currentPermissions = componentsToPermissions.get(component);

        if (currentPermissions == null) {
            currentPermissions = new CopyOnWriteArraySet<>(newPermissions);
            componentsToPermissions.put(component, newPermissions);
        } else {
            currentPermissions.addAll(newPermissions);
        }

        final Map<Component, Set<Object>> componentsToPermissions = singletonMap(component, currentPermissions);

        applyComponents(componentsToPermissions);
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

        checkAccessToCurrentView();
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

        final UI ui = Check.notNull(UI.getCurrent(), "UI.getCurrent() must not return null here");

        final Navigator navigator = Check.notNull(
                ui.getNavigator(),
                "a navigator needs to be registered on the current UI before Authorization.bindView() or Authorization.bindViews() can be called"
        );

        navigator.addViewChangeListener(this);

        registeredAsViewChangeListener = true;
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        requireNonNull(event);

        //null to empty string to avoid NPE
        final String newParameters = event.getParameters() != null ? event.getParameters() : "";

        try {
            final boolean navigationToNewViewAllowed = navigationAllowed(event.getNewView(), newParameters);

            if (!navigationToNewViewAllowed) {
                checkAccessToCurrentView();
            }

            return navigationToNewViewAllowed;
        } finally {
            currentParameters = newParameters;
        }
    }

    void checkAccessToCurrentView() {
        UI ui = Check.notNull(UI.getCurrent());

        final Navigator navigator = ui.getNavigator();

        if (navigator == null) {
            //no navigator present means that there is no view, so we're good
            return;
        }

        final View currentView = navigator.getCurrentView();

        if (currentView != null) {

            final boolean navigationToCurrentViewAllowed = navigationAllowed(currentView, currentParameters);

            if (!navigationToCurrentViewAllowed) {
                //in case the user does not have access to the view he is currently on, go to default view
                navigator.navigateTo("");
            }
        } else {
            //this happens, when the user opens a link to a view he has no access to
            navigator.navigateTo("");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> boolean navigationAllowed(View newView, String parameters) {
        requireNonNull(newView);

        final Set<Object> permissions = viewsToPermissions.getOrDefault(newView, Collections.EMPTY_SET);

        for (Object permission : permissions) {
            if (!isGranted(permission)) {
                return false;
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

    @SuppressWarnings("unchecked")
    <T, F> Registration bindDataStronglyTyped(Class<T> itemClass, Holder<DataProvider<T, F>> dataProviderHolder) {
        requireNonNull(itemClass);
        requireNonNull(dataProviderHolder);

        final Authorizer<T> authorizer = authorizerPool.getAuthorizer(itemClass);

        final DataProvider<T, F> oldDataProvider = dataProviderHolder.get();
        final DataProvider<T, F> newDataProvider = new AuthorizingDataProvider<>(oldDataProvider, authorizer);

        dataProviderHolder.set(newDataProvider);

        dataProviders.add(new WeakReference<>(newDataProvider));

        return new DataRegistration(dataProviderHolder);
    }
}
