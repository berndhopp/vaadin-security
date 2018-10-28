package org.ilay.visibility;

import com.vaadin.flow.component.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation is to be placed on other annotations, marking them as visibility-annotations and
 * assigning {@link VisibilityEvaluator}s to them. Take, for example, the case that a certain {@link
 * Component} is only to be visible to users that have the role 'administrator'. Now the first step
 * would be to create an annotation called VisibleTo and annotate it with VisibilityAnnotation
 *
 * <pre>
 *     &#064;Retention(RUNTIME)
 *     &#064;VisibilityAnnotation(RoleBasedVisibilityEvaluator.class)
 *     public &#064;interface VisibleTo {
 *         UserRole value();
 *     }
 * </pre>
 *
 * The RoleBasedVisibilityEvaluator is an {@link VisibilityEvaluator} that could look something like
 * the following. The first generic parameter for this interface is the type of the annotated
 * component ( or just {@link Component} in most ca Note that the generic type for this
 * VisibilityEvaluator is the type of the annotation and the annotation is the last parameter of
 * 'evaluate'.
 *
 * <pre>
 * class RoleBasedAccessEvaluator implements AccessEvaluator&lt;VisibleTo&gt; {
 *
 *     Supplier&lt;UserRole&gt; userRoleProvider;
 *
 *     &#064;Override
 *     public Access evaluate(Location location, Class&lt;?&gt; navigationTarget, VisibleTo annotation) {
 *         final boolean hasRole = annotation.value().equals(userRoleProvider.get());
 *
 *         return hasRole ? Access.granted() : Access.restricted(UserNotInRoleException.class);
 *     }
 * }
 * </pre>
 *
 * VisibleTo can then be used to prevent users that don't have the required role to enter the
 * route-target by just annotating the respective class
 *
 * <pre>
 *     &#064;Route("adminview")
 *     &#064;VisibleTo(UserRole.Admin)
 *      public class AdminView extends Div {
 *      }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface VisibilityAnnotation {
    /**
     * The {@link VisibilityEvaluator} that is to be assigned to the annotation.
     *
     * @return the {@link VisibilityEvaluator}
     */
    Class<? extends VisibilityEvaluator<?>> value();
}
