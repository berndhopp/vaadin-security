package org.ilay.visibility;

public interface VisibilityEvaluator<PAYLOAD> {

    default boolean evaluateVisibility() {
        return evaluateVisibility(null);
    }

    boolean evaluateVisibility(PAYLOAD PAYLOAD);
}
