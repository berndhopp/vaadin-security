package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * A SecureView is a special kind of {@link View} that parses the given
 * {@link ViewChangeEvent#parameters} into an instance of the type-parameter T.
 *
 * An {@link org.ilay.api.Authorizer} for the type T must be available ( see
 * {@link Authorization#start(Supplier)} and {@link Authorization#start(Set)}).
 * The instance of T returned from {@link SecureView#parse(String)} is then evaluated
 * as a permission and if granted, passed to {@link SecureView#onSuccessfulAuthorization(Object)},
 * otherwise passed to {@link SecureView#onFailedAuthorization(Object)}.
 *
 * OnSuccessfulAuthorization can be used to set up the views content, for example by using
 * {@link CustomComponent#setCompositionRoot(Component)}.
 *
 * @author Bernd Hopp
 */
public abstract class SecureView<T> extends CustomComponent implements View {

    /**
     * parses the given parameters into an instance of T, which is usually some
     * sort of identifier for the content to be displayed in the view.
     *
     * @param parameters the parameters taken from {@link ViewChangeEvent#parameters}
     * @return The parsed instance of T
     * @throws ParseException if the input does not comply to what is expected by the parser
     */
    protected abstract T parse(String parameters) throws ParseException;

    /**
     * this method is called when an instance of T was parsed and passed authorization, i.e.
     * the according {@link org.ilay.api.Authorizer#isGranted(Object)} returned true for the
     * instance of T.
     * @param t the instance of T returned by {@link SecureView#parse(String)}
     */
    protected abstract void onSuccessfulAuthorization(T t);

    /**
     * this method is called when an instance of T was parsed and did not pass authorization,
     * i.e. the according {@link org.ilay.api.Authorizer#isGranted(Object)} returned false for
     * the instance of T.
     * @param t the instance of T returned by {@link SecureView#parse(String)}
     */
    protected void onFailedAuthorization(T t) {
        final Optional<VaadinAbstraction.NavigatorFacade> optionalNavigator = VaadinAbstraction.getNavigatorFacade();

        Check.state(optionalNavigator.isPresent());

        @SuppressWarnings("OptionalGetWithoutIsPresent") final VaadinAbstraction.NavigatorFacade navigator = optionalNavigator.get();

        navigator.navigateTo("");
    }

    /**
     * this method is called when {@link SecureView#parse(String)} threw a {@link ParseException}
     * @param parseException the ParseException thrown by {@link SecureView#parse(String)}.
     */
    protected void onParseException(ParseException parseException) {
        Logger.getAnonymousLogger().warning(parseException.getMessage());
    }

    public final void enter(ViewChangeEvent event) {

        final String parameters = event.getParameters();

        Check.notNullOrEmpty(parameters);

        final T t;

        try {
            t = parse(parameters);
        } catch (ParseException e) {
            onParseException(e);
            return;
        }

        requireNonNull(t, "method SecureView#parse(T) must not return null, throw ParseException instead");

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        if (authorizationContext.evaluate(t)) {
            onSuccessfulAuthorization(t);
        } else {
            onFailedAuthorization(t);
        }
    }

    /**
     * this exception indicates that an {@link ViewChangeEvent#parameters} was not parseable
     * by {@link SecureView#parse(String)}
     */
    public static class ParseException extends Exception {
        public ParseException() {
        }

        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }

        public ParseException(Throwable cause) {
            super(cause);
        }

        public ParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
