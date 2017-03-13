package org.vaadin.security;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;
import org.vaadin.security.api.Evaluator;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class EvaluatorPoolTest {

    static EvaluatorPool createEvaluatorPool(boolean withInt, boolean withString, boolean withObject) {
        Set<Evaluator> evaluators = new HashSet<>();

        if (withInt) {
            evaluators.add(Evaluators.integerEvaluator);
        }

        if (withString) {
            evaluators.add(Evaluators.stringEvaluator);
        }

        if (withObject) {
            evaluators.add(Evaluators.objectEvaluator);
        }

        return new EvaluatorPool(evaluators);
    }

    @Test
    public void matching_evaluators_should_be_found() {
        EvaluatorPool evaluatorPool = createEvaluatorPool(true, true, false);
        assertEquals(Evaluators.stringEvaluator, evaluatorPool.getEvaluator(String.class));
        assertEquals(Evaluators.integerEvaluator, evaluatorPool.getEvaluator(Integer.class));
    }

    @Test
    public void inheritance_matching_should_work() {
        ImmutableSet<Evaluator> evaluators = ImmutableSet.of(Evaluators.fooEvaluator);

        EvaluatorPool evaluatorPool = new EvaluatorPool(evaluators);

        Evaluator<Evaluators.Foo> fooEvaluator = evaluatorPool.getEvaluator(Evaluators.Foo.class);

        assertNotNull(fooEvaluator);
        assertFalse(fooEvaluator.evaluate(new Evaluators.Bar()));
        assertFalse(fooEvaluator.evaluate(new Evaluators.Foo()));
        Evaluator<Evaluators.Bar> barEvaluator = evaluatorPool.getEvaluator(Evaluators.Bar.class);
        assertNotNull(barEvaluator);
        assertFalse(barEvaluator.evaluate(new Evaluators.Bar()));
    }

    @Test
    public void conflicting_evaluators_should_return_best_fit() {
        EvaluatorPool evaluatorPool = createEvaluatorPool(true, true, true);
        final Evaluator<String> evaluator = evaluatorPool.getEvaluator(String.class);
        assertEquals(Evaluators.stringEvaluator, evaluator);
        evaluator.evaluate("");
    }

    @Test
    public void no_matching_evaluator_should_return_best_fit() {
        EvaluatorPool evaluatorPool = createEvaluatorPool(true, false, true);
        final Evaluator<String> evaluator = evaluatorPool.getEvaluator(String.class);
        assertNotNull(evaluator);
        evaluator.evaluate("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void no_matching_evaluators_should_throw_illegal_argument() {
        EvaluatorPool evaluatorPool = createEvaluatorPool(true, true, false);
        evaluatorPool.getEvaluator(Float.class);
    }

}
