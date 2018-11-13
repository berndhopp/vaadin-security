package org.ilay.visibility;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * To enable ilay-visibility, this annotation needs to be attached to your servlet, like
 *
 * {@literal @}EnableIlay {@literal @}WebServlet(urlPatterns = "/*") {@literal
 * @}PackagesToScan("com.mypackage") public class MyServlet extends GuiceVaadinServlet{ }
 */
@Import(PostProcessor.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableIlay {
}
