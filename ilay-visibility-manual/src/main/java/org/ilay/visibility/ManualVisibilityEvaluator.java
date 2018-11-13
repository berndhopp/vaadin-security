package org.ilay.visibility;

import com.vaadin.flow.component.Component;

/**
 * evaluates the visibility of a component for the current user
 *
 * @see IlayVisibility#register(Component, ManualVisibilityEvaluator)
 */
public interface ManualVisibilityEvaluator {
    boolean evaluateVisibility();
}
