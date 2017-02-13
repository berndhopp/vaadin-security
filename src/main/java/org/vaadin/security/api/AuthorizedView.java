package org.vaadin.security.api;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public abstract class AuthorizedView<T> extends CustomComponent implements View {

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

    protected abstract T parse(String parameters) throws ParseException;

    protected abstract boolean checkAuthorization(T t);

    protected abstract void onFailedAuthorization(T t);

    protected abstract void onSuccessfulAuthorization(T t);

    protected void onParseException(ParseException parseException){
        Logger.getAnonymousLogger().warning(parseException.getMessage());
        UI.getCurrent().getNavigator().navigateTo("");
    }

    public final void enter(ViewChangeEvent event){
        try {
            T t = parse(event.getParameters());

            if(t == null){
                throw new NullPointerException("AuthorizedView.parse() must not return null, throw ParseException instead");
            }

            if (checkAuthorization(t)) {
                onSuccessfulAuthorization(t);
            } else {
                onFailedAuthorization(t);
            }
        } catch (ParseException e) {
            onParseException(e);
        }
    }
}
