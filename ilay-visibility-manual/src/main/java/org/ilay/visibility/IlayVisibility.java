package org.ilay.visibility;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

import org.ilay.PermissionsChangedEvent;

import static com.vaadin.flow.component.ComponentUtil.addListener;
import static java.util.Objects.requireNonNull;
import static org.ilay.visibility.VisibilityUtil.evaluateVisibility;
import static org.ilay.visibility.VisibilityUtil.hasVisibilityAnnotation;

/**
 * usage example:
 *
 * <pre>
 *     <code>
 * Button button = new Button();
 *
 * ManualVisibilityEvaluator evaluator = () -> {
 *      return VaadinSession.getCurrent().getAttribute(User.class) != null;
 * };
 *
 * IlayVisibility.register(button, evaluator);
 *    </code>
 * </pre>
 */
@SuppressWarnings("unused")
public final class IlayVisibility {
    private IlayVisibility() {
    }

    /**
     * registers the {@link Component} for visibility-evaluation. The components' class needs to be
     * annotated with one annotation carrying a {@link VisibilityAnnotation}.
     */
    public static void register(Component component) {
        requireNonNull(component);

        final Class<? extends Component> componentClass = component.getClass();

        if (!hasVisibilityAnnotation(componentClass)) {
            throw new IllegalArgumentException(componentClass + " cannot be registered because it has not visibilityAnnotation attached to it ( see org.ilay.visibility.VisibilityAnnotation) ");
        }

        addListener(UI.getCurrent(), PermissionsChangedEvent.class, e -> evaluateVisibility(component));
        evaluateVisibility(component);
    }

    /**
     * registers the {@link Component} for visibility-evaluation.
     */
    public static void register(Component component, ManualVisibilityEvaluator visibilityEvaluator) {
        requireNonNull(component);
        requireNonNull(visibilityEvaluator);

        addListener(UI.getCurrent(), PermissionsChangedEvent.class, e -> component.setVisible(visibilityEvaluator.evaluateVisibility()));
        component.setVisible(visibilityEvaluator.evaluateVisibility());
    }
}
