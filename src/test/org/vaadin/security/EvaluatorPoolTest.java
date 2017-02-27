package org.vaadin.security;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.vaadin.security.api.Evaluator;
import org.vaadin.security.api.EvaluatorPool;
import org.vaadin.security.impl.DefaultEvaluatorPool;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class EvaluatorPoolTest {

    static EvaluatorPool createEvaluatorPool(boolean withInt, boolean withString, boolean withObject) {
        Set<Evaluator<?>> evaluators = new HashSet<>();

        if (withInt) {
            evaluators.add(Evaluators.integerEvaluator);
        }

        if (withString) {
            evaluators.add(Evaluators.stringEvaluator);
        }

        if (withObject) {
            evaluators.add(Evaluators.objectEvaluator);
        }

        return new DefaultEvaluatorPool(evaluators);
    }

    @Test
    public void matching_evaluators_should_be_found() {
        EvaluatorPool evaluatorPool = createEvaluatorPool(true, true, false);
        assertEquals(Evaluators.stringEvaluator, evaluatorPool.getEvaluator(String.class));
        assertEquals(Evaluators.integerEvaluator, evaluatorPool.getEvaluator(Integer.class));
    }

    @Test
    public void inheritance_matching_should_work() {
        ImmutableSet<Evaluator<?>> evaluators = ImmutableSet.of(Evaluators.fooEvaluator);

        EvaluatorPool evaluatorPool = new DefaultEvaluatorPool(evaluators);

        assertEquals(Evaluators.fooEvaluator, evaluatorPool.getEvaluator(Evaluators.Bar.class));
    }

    @Test(expected = IllegalStateException.class)
    public void conflicting_evaluators_should_throw_illegal_state() {
        EvaluatorPool evaluatorPool = createEvaluatorPool(true, true, true);
        evaluatorPool.getEvaluator(String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void no_matching_evaluators_should_throw_illegal_argument() {
        EvaluatorPool evaluatorPool = createEvaluatorPool(true, true, false);
        evaluatorPool.getEvaluator(Float.class);
    }

}
