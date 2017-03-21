package org.ilay;

import com.vaadin.data.HasItems;
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

/**
 * <b>Authorization</b> is the main entry point for the ILAY framework.
 */
@SuppressWarnings("unused")
public final class Authorization {

    private static Supplier<Navigator> navigatorSupplier = () -> UI.getCurrent().getNavigator();
    private static boolean setUp = false;

    private Authorization() {
    }

    public static void start(Set<Evaluator> evaluators) {
        requireNonNull(evaluators);
        start(() -> evaluators);
    }

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

    public static <T> void bindData(Class<T> itemClass, HasItems<T> hasItems) {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        authorizationContext.bindData(itemClass, hasItems);
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

    public static <T> void unbindData(HasItems<T> hasItems) {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        authorizationContext.unbindData(hasItems);
    }

    public static void applyAll() {
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Collection<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();
        apply(componentsToPermissions, authorizationContext);

    }

    public static void apply(Component... components) {
        requireNonNull(components);
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Collection<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();
        final Map<Component, Collection<Object>> reducedComponentsToPermissions = stream(components).collect(toMap(c -> c, componentsToPermissions::get));
        apply(reducedComponentsToPermissions, authorizationContext);
    }

    static void apply(Map<Component, Collection<Object>> componentsToPermissions, AuthorizationContext authorizationContext) {
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
}
