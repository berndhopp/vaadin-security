package org.ilay;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.HasFilterableDataProvider;
import com.vaadin.data.HasItems;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Component;

import org.ilay.api.Authorizer;
import org.ilay.api.Restrict;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * <b>Authorization</b> is the main entry point for the ILAY framework. It provides methods for
 * binding and unbinding {@link Component}s, {@link View}s and {@link HasItems} to permissions as
 * well as applying the bound permissions.
 *
 * The first method that is called on {@link Authorization} needs to be either {@link
 * Authorization#start(Set)} or {@link Authorization#start(Supplier)} <code> Authorizer{@literal
 * <}Foo{@literal >} fooEvaluator = new DataAuthorizer(){ public boolean isGranted(Foo foo){ boolean
 * granted = //evaluation logic goes here return granted; }
 *
 * public Class{@literal <}Foo{@literal >} getPermissionClass(){ return Foo.class; } }
 *
 * Authorizer{@literal <}UserRole{@literal >} userRoleEvaluator = new DataAuthorizer(){ public
 * boolean isGranted(UserRole userRole){ boolean granted = //evaluation logic goes here return
 * granted; }
 *
 * public Class{@literal <}UserRole{@literal >} getPermissionClass(){ return UserRole.class; } }
 *
 * Set{@literal <}Authorizer{@literal >} evaluators = new HashSet{@literal <}{@literal >}();
 *
 * evaluators.add(fooEvaluator); evaluators.add(userRoleEvaluator);
 *
 * //...
 *
 * Authorization.start(evaluators); </code>
 *
 * Then, {@link Component}s, {@link View}s and {@link HasItems}' can be bound with the {@link
 * Authorization#restrictComponents(Component...)}, {@link Authorization#restrictViews(View...)} and
 * {@link Authorization#restrictData(Class, HasDataProvider)} methods.
 *
 * <code> Button button = new Button(); AdminView adminView = new AdminView(); Grid{@literal
 * <}Foo{@literal >} fooGrid = new Grid{@literal <}Foo{@literal >}(Foo.class);
 *
 * Authorization.restrictComponent(button).to(UserRole.USER); Authorization.restrictView(myView).to(UserRole.ADMIN);
 * Authorization.restrictData(fooGrid); </code>
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
public final class Authorization {

    private static final String NOT_INITIALIZED_ERROR_MESSAGE = "Authorization.start() must be called before this method";
    private static Registration registration;

    private Authorization() {
    }

    /**
     * starts the authorization-engine. This method or {@link Authorization#start(Supplier)} must be
     * called before any other method in {@link Authorization} is called. Use this method instead of
     * {@link Authorization#start(Supplier)} if the set of {@link Authorizer}s is immutable and the
     * same set can be used for all {@link com.vaadin.server.VaadinSession}s.
     *
     * @param authorizers the {@link Authorizer}s needed. For every object passed in {@link
     *                    ComponentRestrictRegistration#to(Object...)}, {@link
     *                    ViewRestrictRegistration#to(Object...)}, {@link Authorization#restrictData(Class,
     *                    HasFilterableDataProvider)} or {@link Authorization#restrictData(Class,
     *                    HasDataProvider)} there must be a evaluator in the set where the {@link
     *                    Authorizer#getPermissionClass()} is assignable from the objects {@link
     *                    Class}.
     */
    public static void start(Set<Authorizer> authorizers) {
        Check.notNullOrEmpty(authorizers);
        start(() -> authorizers);
    }

    /**
     * starts the authorization-engine. This method or {@link Authorization#start(Set)} )} must be
     * called before any other method in {@link Authorization} is called. Use this method instead of
     * {@link Authorization#start(Set)} if the set of {@link Authorizer}s is not immutable and a
     * different set may be used for all {@link com.vaadin.server.VaadinSession}s.
     *
     * @param authorizerSupplier the {@link Supplier} for the {@link Authorizer}s needed. For every
     *                           object passed in {@link ComponentRestrictRegistration#to(Object...)},
     *                           {@link ViewRestrictRegistration#to(Object...)}, {@link
     *                           Authorization#restrictData(Class, HasFilterableDataProvider)} or
     *                           {@link Authorization#restrictData(Class, HasDataProvider)} there
     *                           must be a evaluator in the set where the {@link
     *                           Authorizer#getPermissionClass()} is assignable from the objects
     *                           {@link Class}.
     */
    public static void start(Supplier<Set<Authorizer>> authorizerSupplier) {
        requireNonNull(authorizerSupplier);
        Check.state(registration == null, "start() cannot be called more than once");

        final VaadinService vaadinService = Check.notNull(
                VaadinService.getCurrent(),
                "VaadinService must be available before Authorization#start() is called"
        );

        //for every new VaadinSession, we initialize the AuthorizationContext
        registration = vaadinService.addSessionInitListener(
                //for every new VaadinSession, we initialize the AuthorizationContext
                e -> {
                    final Set<Authorizer> authorizers = authorizerSupplier.get();
                    Check.notNullOrEmpty(authorizers);
                    AuthorizationContext.initSession(authorizers);
                }
        );
    }

    static void reset() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    /**
     * returns a {@link ComponentRestrictRegistration} to connect a {@link Component} to one or more
     * permissions
     *
     * <code> Button button = new Button(); Authorization.bindComponent(button).to(Permission.ADMIN);
     * </code>
     *
     * @param component the component to be bound to one or more permission, cannot be null
     * @return a {@link ComponentRestrictRegistration} for a chained fluent API
     */
    public static Restrict restrictComponent(Component component) {
        Check.state(registration != null, NOT_INITIALIZED_ERROR_MESSAGE);
        requireNonNull(component);
        return new ComponentRestrictRegistration(component);
    }

    /**
     * returns a {@link ComponentRestrictRegistration} to connect {@link Component}s to one or more
     * permissions
     *
     * <code> Button button = new Button(); Label label = new Label();
     * Authorization.bindComponents(button, label).to(Permission.ADMIN); </code>
     *
     * @param components the {@link Component}s to be bound to one or more permission, cannot be
     *                   null or empty
     * @return a {@link ComponentRestrictRegistration} for a chained fluent API
     */
    public static Restrict restrictComponents(Component... components) {
        Check.state(registration != null, NOT_INITIALIZED_ERROR_MESSAGE);
        Check.arraySanity(components);
        return new ComponentRestrictRegistration(components);
    }

    /**
     * returns a {@link ViewRestrictRegistration} to connect a {@link View} to one or more
     * permissions
     *
     * <code> View view = createView(); Authorization.bindView(view).to(Permission.ADMIN); </code>
     *
     * @param view the {@link View} to be bound to one or more permission, cannot be null or empty
     * @return a {@link ViewRestrictRegistration} for a chained fluent API
     */
    public static Restrict restrictView(View view) {
        Check.state(registration != null, NOT_INITIALIZED_ERROR_MESSAGE);
        requireNonNull(view);
        return new ViewRestrictRegistration(view);
    }

    /**
     * returns a {@link ViewRestrictRegistration} to connect {@link View}s to one or more
     * permissions
     *
     * <code> View view = createView(); View view2 = createView(); Authorization.bindViews(view,
     * view2).to(Permission.ADMIN); </code>
     *
     * @param views the {@link View}s to be bound to one or more permission, cannot be null or
     *              empty
     * @return a {@link ViewRestrictRegistration} for a chained fluent API
     */
    public static Restrict restrictViews(View... views) {
        Check.state(registration != null, NOT_INITIALIZED_ERROR_MESSAGE);
        Check.arraySanity(views);
        return new ViewRestrictRegistration(views);
    }

    /**
     * binds the data, or items, in the {@link HasFilterableDataProvider} to authorization. Each
     * item t of type T in an HasFilterableDataProvider{@literal <}T, F{@literal >} is it's own
     * permission and will only be displayed when an {@link Authorizer}{@literal <}T, ?{@literal
     * >}'s {@link Authorizer#isGranted(Object)}-method returned true for t. If no {@link
     * Authorizer} for the type T is available, an exception will be thrown.
     *
     * @param itemClass       the class of T ( the item's class )
     * @param hasDataProvider the {@link HasFilterableDataProvider} to be bound
     * @param <T>             the Type of the items
     */
    public static <T, F> Registration restrictData(Class<T> itemClass, HasFilterableDataProvider<T, F> hasDataProvider) {
        Check.state(registration != null, NOT_INITIALIZED_ERROR_MESSAGE);
        requireNonNull(itemClass);
        requireNonNull(hasDataProvider);
        Check.noUnclosedRestrict();

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        Holder<DataProvider<T, F>> holder = Holder.wrap(hasDataProvider);

        return authorizationContext.bindDataStronglyTyped(itemClass, holder);
    }

    /**
     * binds the data, or items, in the {@link HasFilterableDataProvider} to authorization. Each
     * item t of type T in an HasFilterableDataProvider{@literal <}T, F{@literal >} is it's own
     * permission and will only be displayed when an {@link Authorizer}{@literal <}T, ?{@literal
     * >}'s {@link Authorizer#isGranted(Object)}-method returned true for t. If no {@link
     * Authorizer} for the type T is available, an exception will be thrown.
     *
     * @param itemClass       the class of T ( the item's class )
     * @param hasDataProvider the {@link HasFilterableDataProvider} to be bound
     * @param <T>             the Type of the items
     */
    public static <T> Registration restrictData(Class<T> itemClass, HasDataProvider<T> hasDataProvider) {
        Check.state(registration != null, NOT_INITIALIZED_ERROR_MESSAGE);
        requireNonNull(itemClass);
        requireNonNull(hasDataProvider);
        Check.noUnclosedRestrict();

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        Holder<DataProvider<T, ?>> holder = Holder.wrap(hasDataProvider);

        return authorizationContext.bindData(itemClass, holder);
    }

    /**
     * All permissions will be re-evaluated. Call this method when for example the current users
     * roles change or generally whenever there is reason to believe that an {@link Authorizer}
     * would now grant permissions differently than in the past.
     *
     * <code> User user = createUser();
     *
     * user.setRole(Role.USER);
     *
     * Button button = new Button("admin mode");
     *
     * Authorization.bindComponent(button).to(Role.ADMIN);
     *
     * // button.getVisible() == false
     *
     * user.setRole(Role.ADMIN);
     *
     * // button.getVisible() == false
     *
     * Authorization.rebind();
     *
     * // button.getVisible() == true
     *
     * </code>
     */
    public static void reapplyRestrictions() {
        Check.state(registration != null, NOT_INITIALIZED_ERROR_MESSAGE);
        Check.noUnclosedRestrict();
        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();
        final Map<Component, Set<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();
        reapplyInternal(componentsToPermissions, authorizationContext);
    }

    private static void reapplyInternal(Map<Component, Set<Object>> componentsToPermissions, AuthorizationContext authorizationContext) {
        requireNonNull(componentsToPermissions);
        requireNonNull(authorizationContext);

        authorizationContext.applyComponents(componentsToPermissions);
        authorizationContext.applyData();
        authorizationContext.checkAccessToCurrentView();
    }
}
