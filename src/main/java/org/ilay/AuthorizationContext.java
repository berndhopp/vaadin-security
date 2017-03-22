package org.ilay;

import com.vaadin.data.HasItems;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

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

class AuthorizationContext implements ViewChangeListener {

    static Supplier<AuthorizationContext> instanceProvider = new ProductionAuthorizationContextSupplier();
    private final Map<Component, Collection<Object>> componentsToPermissions = new WeakHashMap<>();
    private final Map<View, Collection<Object>> viewsToPermissions = new WeakHashMap<>();
    private final AuthorizerPool authorizerPool;
    private final Map<Component, Boolean> trackedVisibilities = new WeakHashMap<>();
    private final Set<Reference<DataProvider<?, ?>>> dataProviders = new HashSet<>();
    private boolean registeredAsViewChangeListener = false;

    private AuthorizationContext(Set<Authorizer> authorizers) {
        this.authorizerPool = new AuthorizerPool(authorizers);
    }

    static void init(Set<Authorizer> authorizers) {
        requireNonNull(authorizers);
        AuthorizationContext authorizationContext = new AuthorizationContext(authorizers);
        final VaadinSession vaadinSession = VaadinSession.getCurrent();

        requireNonNull(vaadinSession, "no VaadinSession available");

        vaadinSession.setAttribute(AuthorizationContext.class, authorizationContext);
    }

    static AuthorizationContext getCurrent() {
        return instanceProvider.get();
    }

    void setInstanceProvider(Supplier<AuthorizationContext> instanceProvider) {
        AuthorizationContext.instanceProvider = instanceProvider;
    }

    Map<Component, Collection<Object>> getComponentsToPermissions() {
        return componentsToPermissions;
    }

    Map<View, Collection<Object>> getViewsToPermissions() {
        return viewsToPermissions;
    }

    @SuppressWarnings("unchecked")
    <T, F> void bindData(Class<T> itemClass, HasItems<T> hasDataProvider) {
        requireNonNull(itemClass);
        requireNonNull(hasDataProvider);

        final ConfigurableFilterDataProvider<T, Void, F> filterDataProvider = (ConfigurableFilterDataProvider<T, Void, F>) hasDataProvider.getDataProvider().withConfigurableFilter();
        final Authorizer<T, F> authorizer;

        try {
            authorizer = authorizerPool.getAuthorizer(itemClass);
        } catch (ClassCastException e) {
            throw new DataBindingException(e);
        }

        final F filter = requireNonNull(authorizer.asFilter());

        filterDataProvider.setFilter(filter);

        dataProviders.add(new WeakReference<>(filterDataProvider));
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

    <T> void unbindData(HasItems<T> hasItems) {
        requireNonNull(hasItems);
        hasItems.getDataProvider().withConfigurableFilter().setFilter(null);
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

        UI ui = requireNonNull(UI.getCurrent(), "UI.getCurrent() must be present in order to call this method");

        Navigator navigator = requireNonNull(
                ui.getNavigator(),
                "a Navigator must be registered with the current UI by calling UI.getCurrent().setNavigator() in order to call this method."
        );

        navigator.addViewChangeListener(this);

        registeredAsViewChangeListener = true;
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        requireNonNull(event);

        final View newView = requireNonNull(event.getNewView());

        final Collection<Object> permissions = viewsToPermissions.get(newView);

        return permissions == null || permissions.stream().allMatch(this::evaluate);
    }
}
