package org.ilay.visibility;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

import org.ilay.PermissionsChangedEvent;

import static com.vaadin.flow.component.ComponentUtil.addListener;
import static java.util.Objects.requireNonNull;
import static org.ilay.visibility.VisibilityUtil.evaluateVisibility;
import static org.ilay.visibility.VisibilityUtil.hasVisibilityAnnotation;

public final class IlayVisibility {

    public static void register(Component component) {
        requireNonNull(component);

        final Class<? extends Component> componentClass = component.getClass();

        if (!hasVisibilityAnnotation(componentClass)) {
            throw new IllegalArgumentException(componentClass + " cannot be registered because it has not visibilityAnnotation attached to it ( see org.ilay.visibility.VisibilityAnnotation) ");
        }

        addListener(UI.getCurrent(), PermissionsChangedEvent.class, e -> evaluateVisibility(component));
        evaluateVisibility(component);
    }
}


