package org.ilay;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import org.ilay.api.Authorizer;
import org.ilay.api.InMemoryAuthorizer;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();

        final TextField name = new TextField();
        name.setCaption("Type your name here:");

        Button button = new Button("Click Me");

        Authorization.restrictComponent(button).to("user");

        Grid<Object> mygrid = new Grid<>(Object.class);

        Authorization.restrictData(Object.class, mygrid);


        button.addClickListener(e -> {
            layout.addComponent(new Label("Thanks " + name.getValue()
                    + ", it works!"));
        });

        layout.addComponents(name, button);

        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
        @Override
        public void init() throws ServletException {
            super.init();
            InMemoryAuthorizer<String> authorizer = new InMemoryAuthorizer<String>() {
                @Override
                public boolean isGranted(String permission) {


                }

                @Override
                public Class<String> getPermissionClass() {
                    return String.class;
                }
            };

            Set<Authorizer> authorizers = new HashSet<>();

            authorizers.add(authorizer);

            Authorization.start(authorizers);
        }
    }
}
