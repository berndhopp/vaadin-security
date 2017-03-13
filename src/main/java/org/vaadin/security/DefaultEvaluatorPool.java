package org.vaadin.security;

import org.vaadin.security.api.Evaluator;
import org.vaadin.security.api.EvaluatorPool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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

    int calculateDistance(Class<?> parent, Class<?> child) {
        int distance = 0;

        Class<?> clazz = child;

        do {
            clazz = clazz.getSuperclass();
            ++distance;
        } while (!clazz.equals(parent));

        return distance;
    }

    @SuppressWarnings("unchecked")
    private <T> Evaluator<T> findDerived(final Class<T> permissionClass) {
        Evaluator evaluator = null;

        for (Evaluator<?> anEvaluator : evaluators.values()) {
            if (anEvaluator.getPermissionClass().isAssignableFrom(permissionClass)) {
                if (evaluator != null) {
                    int distanceOld = calculateDistance(permissionClass, evaluator.getPermissionClass());
                    int distanceNew = calculateDistance(permissionClass, anEvaluator.getPermissionClass());

                    if (distanceOld > distanceNew) {
                        evaluator = anEvaluator;
                    }
                } else {
                    evaluator = anEvaluator;
                }
            }
        }

        checkArgument(evaluator != null, "no evaluator found for %s", permissionClass);

        final Evaluator finalEvaluator = evaluator;

        return new Evaluator<T>() {
            @Override
            public boolean evaluate(T permission) {
                return finalEvaluator.evaluate(permission);
            }

            @Override
            public Class<T> getPermissionClass() {
                return finalEvaluator.getPermissionClass();
            }
        };
    }
}
