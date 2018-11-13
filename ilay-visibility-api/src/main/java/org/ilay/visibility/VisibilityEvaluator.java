package org.ilay.visibility;

import java.lang.annotation.Annotation;

/**
 * Evaluates, whether the current user is granted visibility to the annotated {@link
 * com.vaadin.flow.component.Component}.
 *
 * usage example
 *
 * <code>
 * {@literal @}VisibilityAnnotation(AdminOnlyVisibilityEvaluator.class) {@literal
 * @}Retention(RUNTIME) {@literal @}Target(ElementType.TYPE) public {@literal @}interface AdminOnly
 * { }
 *
 * public class AdminOnlyVisibilityEvaluator implements VisibilityEvaluator{@literal
 * <}AdminOnly{@literal >}{ boolean evaluateVisibility(AdminOnly annotation){ User user =
 * VaadinSession.getCurrent().getAttribute(User.class);
 *
 * return user != null && user.isAdmin(); } }
 *
 * {@literal @}AdminOnly public class AdminOnlyButton extends Button{ ... }
 *
 * <h3>IMPORTANT</h3>
 *
 * Never forget to call {@link org.ilay.PermissionsChangedEvent#fire} when a user has logged in,
 * logged out or when his permissions changed:
 *
 * //onLogin is an example method, it's just 'the place where a user has logged in' void
 * onLogin(User loggedInUser){ PermissionsChangedEvent.fire(); }
 * </code>
 */
public interface VisibilityEvaluator<ANNOTATION extends Annotation> {
    boolean evaluateVisibility(ANNOTATION annotation);
}
