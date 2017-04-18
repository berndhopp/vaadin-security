package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A TypedAuthorizationView is a special kind of {@link View} that parses the given
 * {@link ViewChangeEvent#parameters} into an instance of the type-parameter T.
 *
 * An {@link org.ilay.api.Authorizer} for the type T must be available ( see
 * {@link Authorization#start(Supplier)} and {@link Authorization#start(Set)}).
 * The instance of T returned from {@link TypedAuthorizationView#parse(String)} is then evaluated
 * as a permission and if granted, passed to {@link TypedAuthorizationView#enter(Object)}.
 *
 * OnSuccessfulAuthorization can be used to set up the views content, for example by using
 * {@link CustomComponent#setCompositionRoot(Component)}.
 * <code>
 *     class MySecureItemsView extends TypedAuthorizationView{@literal <}ItemId{@literal >} {
 *
 *         {@literal @}Overwrite
 *         protected ItemId parse(String parameters){
 *             return ItemId.parse(parameters);
 *         }
 *
 *         {@literal @}Overwrite
 *         protected void enter(ItemId itemId){
 *             Item item = ItemDao.getItem(itemId);
 *
 *             //make components display the item
 *             ...
 *         }
 *     }
 * </code>
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
public abstract class TypedAuthorizationView<T> extends CustomComponent implements View {

    protected TypedAuthorizationView() {
        AuthorizationContext.getCurrent().ensureViewChangeListenerRegistered();
    }

    /**
     * parses the given parameters into an instance of T, which is usually some
     * sort of identifier for the content to be displayed in the view.
     * @param parameters the parameters taken from {@link ViewChangeEvent#parameters}
     * @return The parsed instance of T
     * @throws ParseException if the input does not comply to what is expected by the parser
     */
    protected abstract T parse(String parameters) throws ParseException;

    /**
     * this method is called when an instance of T was parsed and passed authorization, i.e.
     * the according {@link org.ilay.api.Authorizer#isGranted(Object)} returned true for the
     * instance of T.
     * @param t the instance of T returned by {@link TypedAuthorizationView#parse(String)}
     */
    protected abstract void enter(T t);

    public final void enter(ViewChangeEvent event) {
        //nothing to do here, view has been set up in AuthorizationContext#beforeViewChange
    }

    /**
     * this exception indicates that an {@link ViewChangeEvent#parameters} was not parsable
     * by {@link TypedAuthorizationView#parse(String)}
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
