package org.ilay;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasItems;
import com.vaadin.navigator.View;
import com.vaadin.ui.Component;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * <b>Authorization</b> is the main entry point for the ILAY framework.
 * It provides methods for binding and unbinding {@link Component}s, {@link View}s
 * and {@link HasItems} to permissions as well as applying the bound permissions.
 *
 * The first method that is called on {@link Authorization} needs to be either {@link Authorization#start(Set)}
 * or {@link Authorization#start(Supplier)}
 * <code>
 *     Authorizer{@literal <}Foo{@literal >} fooEvaluator = new InMemoryAuthorizer(){
 *         public boolean isGranted(Foo foo){
 *             boolean granted = //evaluation logic goes here
 *             return granted;
 *         }
 *
 *         public Class{@literal <}Foo{@literal >} getPermissionClass(){
 *             return Foo.class;
 *         }
 *     }
 *
 *     Authorizer{@literal <}UserRole{@literal >} userRoleEvaluator = new InMemoryAuthorizer(){
 *         public boolean isGranted(UserRole userRole){
 *             boolean granted = //evaluation logic goes here
 *             return granted;
 *         }
 *
 *         public Class{@literal <}UserRole{@literal >} getPermissionClass(){
 *             return UserRole.class;
 *         }
 *     }
 *
 *     Set{@literal <}Authorizer{@literal >} evaluators = new HashSet{@literal <}{@literal >}();
 *
 *     evaluators.add(fooEvaluator);
 *     evaluators.add(userRoleEvaluator);
 *
 *     //...
 *
 *     Authorization.start(evaluators);
 * </code>
 *
 * Then, {@link Component}s, {@link View}s and {@link HasItems}' can be bound with
 * the {@link Authorization#bindComponents(Component...)}, {@link Authorization#bindViews(View...)} and
 * {@link Authorization#bindData(Class, HasDataProvider)} methods.
 *
 * <code>
 *     Button button = new Button();
 *     AdminView adminView = new AdminView();
 *     Grid{@literal <}Foo{@literal >} fooGrid = new Grid{@literal <}Foo{@literal >}(Foo.class);
 *
 *     Authorization.bindComponent(button).to(UserRole.USER);
 *     Authorization.bindView(myView).to(UserRole.ADMIN);
 *     Authorization.bindData(fooGrid);
 * </code>
 */
@SuppressWarnings("unused")
public final class Authorization {

    private static final String NOT_INITIALIZED_ERROR_MESSAGE = "Authorization.start() must be called before this method";
    static Supplier<NavigatorFacade> navigatorSupplier = new ProductionNavigatorFacadeSupplier();
    static Supplier<SessionInitNotifier> sessionInitNotifierSupplier = new ProductionSessionInitNotifierSupplier();
    private static boolean initialized = false;

    private Authorization() {
    }

    /**
     * starts the authorization-engine. This method or {@link Authorization#start(Supplier)} must be
     * called before any other method in {@link Authorization} is called. Use this method instead of
     * {@link Authorization#start(Supplier)} if the set of {@link Authorizer}s is immutable and the
     * same set can be used for all {@link com.vaadin.server.VaadinSession}s.
     *
     * @param authorizers the {@link Authorizer}s needed. For every object passed in {@link
     *                    ComponentBind#to(Object...)}, there must be a evaluator in the set where the {@link
     *                    Authorizer#getPermissionClass()} is assignable from the objects {@link
     *                    Class}.
     */
    public static void start(Set<Authorizer> authorizers) {
        Check.notEmpty(authorizers);
        start(() -> authorizers);
    }

    /**
     * starts the authorization-engine. This method or {@link Authorization#start(Set)} )} must be
     * called before any other method in {@link Authorization} is called. Use this method instead of
     * {@link Authorization#start(Set)} if the set of {@link Authorizer}s is not immutable and a different
     * set may be used for all {@link com.vaadin.server.VaadinSession}s.
     * @param evaluatorSupplier the {@link Authorizer}s needed. For every object passed in {@link ComponentBind#to(Object...)}, there
     * must be a evaluator in the set where the {@link Authorizer#getPermissionClass()} is assignable from the objects {@link Class}.
     */
    public static void start(Supplier<Set<Authorizer>> evaluatorSupplier) {
        requireNonNull(evaluatorSupplier);

        if (initialized) {
            throw new IllegalStateException("start() cannot be called more than once");
        }

        final SessionInitNotifier sessionInitNotifier = sessionInitNotifierSupplier.get();

        sessionInitNotifier.addSessionInitListener(
                //for every new VaadinSession, we initialize the AuthorizationContext
                e -> AuthorizationContext.init(Check.notEmpty(evaluatorSupplier.get()))
        );

        initialized = true;
    }

    /**
     *
     * @param component
     * @return
     */
    public static ComponentBind bindComponent(Component component) {
        requireNonNull(component);
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        return bindComponents(component);
    }

    public static ComponentBind bindComponents(Component... components) {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        return new ComponentBind(components);
    }

    public static ViewBind bindView(View view) {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        return bindViews(view);
    }

    public static ViewBind bindViews(View... views) {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        AuthorizationContext.getCurrent().ensureViewChangeListenerRegistered();

        return new ViewBind(views);
    }

    public static <T> void bindData(Class<T> itemClass, HasDataProvider<T> hasItems) {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        authorizationContext.bindData(itemClass, hasItems);
    }

    public static ComponentUnbind unbindComponent(Component component) {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        requireNonNull(component);
        return unbindComponents(component);
    }

    public static ComponentUnbind unbindComponents(Component... components) {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        return new ComponentUnbind(components);
    }

    public static ViewUnbind unbindView(View view) {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        requireNonNull(view);
        return unbindViews(view);
    }

    public static ViewUnbind unbindViews(View... views) {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        return new ViewUnbind(views);
    }

    public static void applyAll() {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Set<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();
        apply(componentsToPermissions, authorizationContext);
    }

    public static void apply(Component... components) {
        Check.state(initialized, NOT_INITIALIZED_ERROR_MESSAGE);
        requireNonNull(components);
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        apply(components, authorizationContext);
    }

    static void apply(Component[] components, AuthorizationContext authorizationContext){
        final Map<Component, Set<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();
        final Map<Component, Set<Object>> reducedComponentsToPermissions = stream(components).collect(toMap(c -> c, componentsToPermissions::get));
        apply(reducedComponentsToPermissions, authorizationContext);
    }

    static void apply(Map<Component, Set<Object>> componentsToPermissions, AuthorizationContext authorizationContext) {
        authorizationContext.applyComponents(componentsToPermissions);
        authorizationContext.applyData();
        reEvaluateCurrentViewAccess();
    }

    private static void reEvaluateCurrentViewAccess() {
        final NavigatorFacade navigator = navigatorSupplier.get();

        if (navigator == null) {
            //no navigator -> no views to check
            return;
        }

        final String state = navigator.getState();
        navigator.navigateTo("");
        navigator.navigateTo(state);
    }
}
