package org.vaadin.security;

import org.vaadin.security.api.Evaluator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class EvaluatorPool {

    private final Map<Class<?>, Evaluator<?>> evaluators;

    public EvaluatorPool(Collection<Evaluator> evaluators) {
        requireNonNull(evaluators);
        this.evaluators = new HashMap<>(evaluators.size());

        for (Evaluator evaluator : evaluators) {
            Evaluator<?> alreadyRegistered = this.evaluators.put(evaluator.getPermissionClass(), evaluator);

            if (alreadyRegistered != null) {
                throw new IllegalStateException("multiple evaluators for class %s" + evaluator.getPermissionClass());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Evaluator<T> getEvaluator(Class<T> permissionClass) {

        requireNonNull(permissionClass);

        Evaluator<T> evaluator = (Evaluator<T>) evaluators.get(permissionClass);

        if (evaluator != null) {
            return evaluator;
        }

        evaluator = findSuperClassEvaluator(permissionClass);

        evaluators.put(permissionClass, evaluator);

        return evaluator;
    }

    @SuppressWarnings("unchecked")
    private <T> Evaluator<T> findSuperClassEvaluator(Class<T> permissionClass) {

        Evaluator<?> evaluator;

        Class<?> clazz = permissionClass;

        do {
            clazz = clazz.getSuperclass();
            evaluator = evaluators.get(clazz);
        } while (evaluator == null && !clazz.equals(Object.class));

        if (evaluator == null) {
            throw new IllegalArgumentException("no evaluator found for %s" + permissionClass);
        }

        final Evaluator finalEvaluator = evaluator;

        //if the evaluator can handle x, it can also handle all subclasses of x
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
