package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;

import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * @author Bernd Hopp
 */
public abstract class SecureView<T> extends CustomComponent implements View {

    protected abstract T parse(String parameters) throws ParseException;

    protected abstract void onSuccessfulAuthorization(T t);

    protected void onFailedAuthorization(T t) {
        Authorization.navigatorSupplier.get().navigateTo("");
    }

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
