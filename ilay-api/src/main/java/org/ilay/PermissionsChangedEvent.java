package org.ilay;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;

/**
 * Fired when permissions have changed. This is usually the case on login/logout, or when the
 * current user has been granted or revoked certain permissions.
 *
 * {@link PermissionsChangedEvent#fire()}}
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PermissionsChangedEvent extends ComponentEvent<UI> {
    private static final long serialVersionUID = -668093721644293986L;

    private PermissionsChangedEvent() {
        super(UI.getCurrent(), false);
    }

    /**
     * Fires a {@link PermissionsChangedEvent} on the {@link com.vaadin.flow.component.ComponentEventBus}
     * of the current {@link UI}. Ilay is depending on this mechanism to communicate changes in
     * permissions of the current user, so this method should be called whenever the current user
     * changes by login or logout or whenever his permissions change, that is when a new permission
     * is granted or an existing permission if revoked from the current user.
     *
     * <code>
     * Button loginButton = new Button("Login", e -> { //The user-class is not defined by ilay.
     * User-authentication is not part of ilay, //you may have a look at {@see
     * https://github.com/berndhopp/joscha} for //OAuth-based authentication for Vaadin. User user =
     * ...
     *
     * //the current user is usually stored in the Vaadin-session, but this is not mandatory
     * VaadinSession.getCurrent().setAttribute(User.class, user);
     *
     * //the change is being communicated PermissionChangedEvent.fire(); });
     * </code>
     */
    public static void fire() {
        ComponentUtil.fireEvent(UI.getCurrent(), new PermissionsChangedEvent());
    }
}
