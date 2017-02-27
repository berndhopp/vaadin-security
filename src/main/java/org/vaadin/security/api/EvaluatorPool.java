package org.vaadin.security.api;

public interface EvaluatorPool {
    <T> Evaluator<? extends T> getEvaluator(Class<T> permissionClass);
}
