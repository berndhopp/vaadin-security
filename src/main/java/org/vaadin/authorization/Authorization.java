package org.vaadin.authorization;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasFilterableDataProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("unused")
public final class Authorization {

    private static Supplier<Navigator> navigatorSupplier = () -> UI.getCurrent().getNavigator();
    private static boolean setUp = false;

    private Authorization() {
    }

    @SuppressWarnings("unused")
    public static void start(Supplier<Set<Evaluator>> evaluatorSupplier) {
        requireNonNull(evaluatorSupplier);

        final VaadinService vaadinService = VaadinService.getCurrent();

        if (vaadinService == null) {
            throw new IllegalStateException("VaadinService is not initialized yet");
        }

        if (setUp) {
            throw new IllegalStateException("setUp() cannot be called more than once");
        }

        vaadinService.addSessionInitListener(
                //for every new VaadinSession, we initialize the AuthorizationContext
                e -> AuthorizationContext.init(evaluatorSupplier.get())
        );

        setUp = true;
    }

    public static Bind bindComponent(Component component) {
        requireNonNull(component);
        return bindComponents(component);
    }

    public static Bind bindComponents(Component... components) {
        return new BindImpl(components);
    }

    public static Bind bindView(View view) {
        return bindViews(view);
    }

    public static Bind bindViews(View... views) {
        return new ViewBindImpl(views);
    }

    @SuppressWarnings("unchecked")
    public static <T, F> void bindHasDataProvider(HasDataProvider<T> hasDataProvider) {
        requireNonNull(hasDataProvider);
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        authorizationContext.bindHasDataProvider(hasDataProvider);
    }

    @SuppressWarnings("unchecked")
    public static <T, F> void bindHasDataProvider(HasFilterableDataProvider<T, F> hasFilterableDataProvider) {
        requireNonNull(hasFilterableDataProvider);

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        authorizationContext.bindHasDataProvider(hasFilterableDataProvider);
    }

    public static Unbind unbindComponent(Component component) {
        requireNonNull(component);
        return unbindComponents(component);
    }

    public static Unbind unbindComponents(Component... components) {
        return new UnbindImpl(components);
    }

    public static Unbind unbindView(View view) {
        requireNonNull(view);
        return unbindViews(view);
    }

    public static Unbind unbindViews(View... views) {
        return new ViewUnbindImpl(views);
    }

    @SuppressWarnings("unchecked")
    public static <T, F> boolean unbindHasDataProvider(HasFilterableDataProvider<T, F> hasFilterableDataProvider) {
        requireNonNull(hasFilterableDataProvider);
        return AuthorizationContext.getCurrent().unbindHasDataProvider(hasFilterableDataProvider);
    }

    @SuppressWarnings("unchecked")
    public static <T, F> boolean unbindHasDataProvider(HasDataProvider<T> hasDataProvider) {
        requireNonNull(hasDataProvider);
        return AuthorizationContext.getCurrent().unbindHasDataProvider(hasDataProvider);
    }

    public static void applyAll() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Collection<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();
        applyInternal(componentsToPermissions, authorizationContext);

    }

    public static void apply(Component... components) {
        requireNonNull(components);
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Collection<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();
        final Map<Component, Collection<Object>> reducedComponentsToPermissions = stream(components).collect(toMap(c -> c, componentsToPermissions::get));
        applyInternal(reducedComponentsToPermissions, authorizationContext);
    }

    static void applyInternal(Map<Component, Collection<Object>> componentsToPermissions, AuthorizationContext authorizationContext) {
        authorizationContext.applyComponents(componentsToPermissions);
        authorizationContext.applyData();
        reEvaluateCurrentViewAccess();
    }

    private static void reEvaluateCurrentViewAccess() {
        final Navigator navigator = navigatorSupplier.get();

        if (navigator == null) {
            //no navigator -> no views to check
            return;
        }

        final String state = navigator.getState();
        navigator.navigateTo("");
        navigator.navigateTo(state);
    }

    void setNavigatorSupplier(Supplier<Navigator> navigatorSupplier) {
        Authorization.navigatorSupplier = navigatorSupplier;
    }

    public interface Bind {
        void to(Object... permission);
    }

    public interface Unbind {
        void from(Object... permissions);

        void fromAll();
    }

    /**
     * Evaluator is the object responsible of deciding if a certain permission is granted in the
     * current context or not. Usually the "current context" is the currently logged in user and
     * it's roles. A "permission" can be any object that is an instance of the generic type argument
     * T, so that every {@link Evaluator} is responsible for evaluating the permissions that are
     * assignable to the type T.
     *
     * @author Bernd Hopp
     */
    public interface Evaluator<T> {

        /**
         * evaluate if a certain permission is granted in the current context
         *
         * @param permission the permission
         * @return true if the permission is granted, otherwise false
         */
        boolean evaluate(T permission);

        /**
         * returns the class of the permission that can be evaluated ( type-parameter T )
         *
         * @return the class of T
         */
        Class<T> getPermissionClass();
    }
}
