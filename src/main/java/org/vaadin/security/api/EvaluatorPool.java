package org.vaadin.security.api;

public interface EvaluatorPool {
    <T> Evaluator<T> getEvaluator(Class<T> permissionClass);
}
