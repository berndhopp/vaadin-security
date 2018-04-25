package org.ilay;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be placed on other annotations, marking them as restriction-annotations and
 * assigning {@link AccessEvaluator}s to them. Take for example the case that a certain route-target
 * is only to be accessed by users that have the role 'administrator'. Now the first step would be
 * to create an annotation called VisibleTo and annotate it with RestrictionAnnotation
 *
 * <pre>
 *     &#064;RestrictionAnnotation(RoleBasedAccessEvaluator.class)
 *     public &#064;interface VisibleTo {
 *         UserRole value();
 *     }
 * </pre>
 *
 * The RoleBasedAccessEvaluator is an {@link AccessEvaluator} that could look something like the
 * following. Note that the generic type for this AccessEvaluator is the type of the annotation
 * and the annotation is the last parameter of 'evaluate'.
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
 * VisibleTo can then be used to prevent users that don't have the required role to enter
 * the route-target by just annotating the respective class
 *
 * <pre>
 *     &#064;Route("adminview")
 *     &#064;VisibleTo(UserRole.Admin)
 *      public class AdminView extends Div {
 *      }
 * </pre>
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface RestrictionAnnotation {
    /**
     * The {@link AccessEvaluator} that is to be assigned to the annotation.
     *
     * @return the {@link AccessEvaluator}
     */
    Class<? extends AccessEvaluator<?>> value();
}
