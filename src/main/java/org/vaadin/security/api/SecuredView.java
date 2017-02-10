package org.vaadin.security.api;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;


/**
 * An abstract class to inherit from for {@link View}'s that have restricted access. It is the SecuredView's responsibility
 * to decide whether access is granted with the current parameters or not, by implementing {@link SecuredView#enterSecured(ViewChangeListener.ViewChangeEvent)}.
 *
 * @author Bernd Hopp
 */
@SuppressWarnings("unused")
public abstract class SecuredView<T extends Component> extends CustomComponent implements View {

    protected SecuredView(T content){
        if(content == null){
            throw new IllegalArgumentException("content cannot be null");
        }

        setCompositionRoot(content);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T getCompositionRoot() {
        return (T)super.getCompositionRoot();
    }

    public final void enter(ViewChangeEvent event){
        if(!enterSecured(event)){
            UI.getCurrent().getNavigator().navigateTo("");
        }
    }

    /**
     * This view is navigated to.
     *
     * This method is always called before the view is shown on screen.
     * {@link ViewChangeEvent#getParameters() event.getParameters()} may contain
     * extra parameters relevant to the view.
     *
     * @param event
     *            ViewChangeEvent representing the view change that is
     *            occurring. {@link ViewChangeEvent#getNewView()
     *            event.getNewView()} returns <code>this</code>.
     * @return true if access to this view is granted, otherwise false. If false is returned, the application will
     * navigate to the default page.
     */
    public abstract boolean enterSecured(ViewChangeEvent event);
}
