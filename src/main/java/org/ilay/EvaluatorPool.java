package org.ilay;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

class EvaluatorPool {

    private final Map<Class<?>, Evaluator<?, ?>> evaluators;

    EvaluatorPool(Collection<Evaluator> evaluators) {
        requireNonNull(evaluators);
        this.evaluators = new HashMap<>(evaluators.size());

        for (Evaluator evaluator : evaluators) {
            requireNonNull(evaluator);
            requireNonNull(evaluator.getPermissionClass());

            Evaluator<?, ?> alreadyRegistered = this.evaluators.put(evaluator.getPermissionClass(), evaluator);

            if (alreadyRegistered != null) {
                throw new ConflictingEvaluatorsException(evaluator, alreadyRegistered, evaluator.getPermissionClass());
            }
        }
    }

    @SuppressWarnings("unchecked")
    <T> Evaluator<T, ?> getEvaluator(Class<T> permissionClass) {

        requireNonNull(permissionClass);

        Evaluator<T, ?> evaluator = (Evaluator<T, ?>) evaluators.get(permissionClass);

        if (evaluator != null) {
            return evaluator;
        }

        for (Evaluator<?, ?> anEvaluator : evaluators.values()) {
            if (anEvaluator.getPermissionClass().isAssignableFrom(permissionClass)) {
                if (evaluator != null) {
                    throw new ConflictingEvaluatorsException(evaluator, anEvaluator, permissionClass);
                }

                evaluator = (Evaluator<T, ?>) anEvaluator;
            }
        }

        requireNonNull(evaluator, "no evaluator found for " + permissionClass);

        evaluators.put(permissionClass, evaluator);

        return evaluator;
    }
}
