package org.ilay.visibility;

import com.vaadin.guice.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * To enable ilay-visibility, this annotation needs to be attached to your servlet, like
 *
 * {@literal @}EnableIlay {@literal @}WebServlet(urlPatterns = "/*") {@literal
 * @}PackagesToScan("com.mypackage") public class MyServlet extends GuiceVaadinServlet{ }
 */
@Retention(RetentionPolicy.RUNTIME)
@Import(IlayModule.class)
public @interface EnableIlay {
}
