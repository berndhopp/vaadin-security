package org.vaadin.security.impl;

import org.vaadin.security.api.Evaluator;
import org.vaadin.security.api.EvaluatorPool;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class DefaultEvaluatorPool implements EvaluatorPool {

    private final Set<Evaluator> evaluators;

    public DefaultEvaluatorPool(Set<Evaluator> evaluators) {
        checkNotNull(evaluators);
        checkArgument(!evaluators.isEmpty());
        this.evaluators = evaluators;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Evaluator<? extends T> getEvaluator(Class<T> permissionClass) {

        checkNotNull(permissionClass);

        Evaluator<? extends T> evaluator = null;

        for (Evaluator<?> anEvaluator : evaluators) {
            if(anEvaluator.getPermissionClass().isAssignableFrom(permissionClass)){
                checkState(
                    evaluator == null,
                    "%s and %s are both assignable to %s",
                    evaluator != null ? evaluator.getClass() : null,
                    anEvaluator.getClass(),
                    permissionClass
                );

                evaluator = (Evaluator<? extends T>)anEvaluator;
            }
        }

        checkArgument(
            evaluator != null,
            "no evaluator found for %s",
            permissionClass
        );

        return evaluator;
    }
}
