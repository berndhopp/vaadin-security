package org.vaadin.security;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;

import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("unused")
public abstract class AuthorizedView<T> extends CustomComponent implements View {

    protected String getPermissionDeniedViewName() {
        return "";
    }

    public String getBadParamsViewName() {
        return "";
    }

    protected abstract T parse(String parameters) throws ParseException;

    boolean checkAuthorization(T t) {
        final VaadinSession vaadinSession = VaadinSession.getCurrent();

        checkState(vaadinSession != null, "VaadinSession.getCurrent() must not return null here");

        final AuthorizationEngine authorizationEngine = vaadinSession.getAttribute(AuthorizationEngine.class);

        checkState(authorizationEngine != null,
                "please call AuthorizationEngine.start() at the bootstrap of your application, or look " +
                        "up how to integrate vaadin authorization with your DI framework ( Spring, Guice, Java CDI )"
        );

        return authorizationEngine.evaluate(t);
    }

    protected void onFailedAuthorization(T t) {
    }

    protected abstract void onSuccessfulAuthorization(T t);

    protected void onParseException(ParseException parseException) {
        Logger.getAnonymousLogger().warning(parseException.getMessage());
    }

    public final void enter(ViewChangeEvent event) {
        try {
            T t = parse(event.getParameters());

            if (t == null) {
                throw new NullPointerException("AuthorizedView.parse() must not return null, throw ParseException instead");
            }

            if (checkAuthorization(t)) {
                onSuccessfulAuthorization(t);
            } else {
                onFailedAuthorization(t);
                UI.getCurrent().getNavigator().navigateTo(getPermissionDeniedViewName());
            }
        } catch (ParseException e) {
            onParseException(e);
            UI.getCurrent().getNavigator().navigateTo(getBadParamsViewName());
        }
    }

    protected static class ParseException extends Exception {
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
