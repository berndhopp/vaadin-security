package org.vaadin.security.impl;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;

import org.vaadin.security.api.PermissionEnforcer;
import org.vaadin.security.api.PermissionEvaluator;

import java.util.HashMap;
import java.util.Map;

public class DefaultPermissionEnforcer implements PermissionEnforcer{

    private final PermissionEvaluator permissionEvaluator;

    private final class RestrictedComponentsSet extends HashMap<Component, String> {}

    public DefaultPermissionEnforcer(PermissionEvaluator permissionEvaluator){
        if(permissionEvaluator == null) throw new IllegalArgumentException("permissionEvaluator cannot be null");
        this.permissionEvaluator = permissionEvaluator;
    }

    public void enforce() {

        RestrictedComponentsSet restrictedComponentsSet = VaadinSession.getCurrent().getAttribute(RestrictedComponentsSet.class);

        if(restrictedComponentsSet != null) {
            for (Map.Entry<Component, String> entry : restrictedComponentsSet.entrySet()) {
                final String permission = entry.getValue();
                final Component component = entry.getKey();

                component.setVisible(permissionEvaluator.hasPermission(permission));
            }
        }
    }

    public void register(Component component, String permission) {
        if(component == null) throw new IllegalArgumentException("component cannot be null");
        if(permission == null || permission.equals("")) throw new IllegalArgumentException("permission cannot be null or empty");

        RestrictedComponentsSet restrictedComponentsSet = VaadinSession.getCurrent().getAttribute(RestrictedComponentsSet.class);

        if(restrictedComponentsSet == null){
            restrictedComponentsSet = new RestrictedComponentsSet();
            VaadinSession.getCurrent().setAttribute(RestrictedComponentsSet.class, restrictedComponentsSet);
        }

        restrictedComponentsSet.put(component, permission);
    }
}
