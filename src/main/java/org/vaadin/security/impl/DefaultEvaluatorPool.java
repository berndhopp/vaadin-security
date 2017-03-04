package org.vaadin.security.impl;

import org.vaadin.security.api.Evaluator;
import org.vaadin.security.api.EvaluatorPool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class DefaultEvaluatorPool implements EvaluatorPool {

    private final Map<Class<?>, Evaluator<?>> evaluators;

    public DefaultEvaluatorPool(Collection<Evaluator> evaluators) {
        checkNotNull(evaluators);
        this.evaluators = new HashMap<>(evaluators.size());

        for (Evaluator evaluator : evaluators) {
            this.evaluators.put(evaluator.getPermissionClass(), evaluator);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Evaluator<T> getEvaluator(Class<T> permissionClass) {

        checkNotNull(permissionClass);

        Evaluator<T> evaluator = (Evaluator<T>) evaluators.get(permissionClass);

        if (evaluator != null) {
            return evaluator;
        }

        evaluator = findDerived(permissionClass);

        evaluators.put(permissionClass, evaluator);

        return evaluator;
    }

    @SuppressWarnings("unchecked")
    private <T> Evaluator<T> findDerived(final Class<T> permissionClass) {
        Evaluator<T> evaluator = null;

        for (Evaluator<?> anEvaluator : evaluators.values()) {
            if (anEvaluator.getPermissionClass().isAssignableFrom(permissionClass)) {
                checkState(
                        evaluator == null,
                        "%s and %s are both assignable to %s",
                        evaluator != null ? evaluator.getClass() : null,
                        anEvaluator.getClass(),
                        permissionClass
                );

                evaluator = new Evaluator<T>() {
                    @Override
                    public boolean evaluate(T permission) {
                        return ((Evaluator) anEvaluator).evaluate(permission);
                    }

                    @Override
                    public Class<T> getPermissionClass() {
                        return permissionClass;
                    }
                };
            }
        }

        checkArgument(evaluator != null, "no evaluator found for %s", permissionClass);

        return evaluator;
    }
}
