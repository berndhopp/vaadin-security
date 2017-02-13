package org.vaadin.security.annotation;

import com.vaadin.ui.Component;
import org.vaadin.security.api.Binder.Bind;

import java.lang.annotation.*;

/**
 * This annotation binds vaadin-security to Dependency-Injection frameworks like Spring, Guice or Java CDI.
 * It may be attached to a {@link Component}, effectively calling
 * {@link org.vaadin.security.api.Binder#bind(Component...)} with the annotated component instance and
 * {@link Bind#to(Object...)} with {@link Restricted#value()}.
 *
 * the following snippets of code are interchargeable.
 *
 * <pre>
 *     <code>
 *          @Restricted("administrator_only")
 *          class AdminOnlyButton extends Button {
 *              ...
 *          }
 *     </code>
 * </pre>
 *
 * <pre>
 *     <code>
 *          class AdminOnlyButton extends Button {
 *               @Inject // or @Autowired in case you use Spring
 *               AdminOnlyButton(Binder binder){
 *                   binder.bind(this).to("administrator_only");
 *               }
 *          }
 *     </code>
 * </pre>
 *
 * See the implementation ( Spring, Guice, CDI ) for details how to set up the integration properly
 *
 * @author Bernd Hopp
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@SuppressWarnings("unused")
public @interface Restricted {
    /**
     * the permission needed to see the component or access the view
     */
    String value();
}
