package org.ilay;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class AuthorizerPoolTest {

    static AuthorizerPool createEvaluatorPool(boolean withInt, boolean withString, boolean withObject) {
        Set<Authorizer> authorizers = new HashSet<>();

        if (withInt) {
            authorizers.add(Evaluators.INTEGER_AUTHORIZER);
        }

        if (withString) {
            authorizers.add(Evaluators.STRING_AUTHORIZER);
        }

        if (withObject) {
            authorizers.add(Evaluators.OBJECT_AUTHORIZER);
        }

        return new AuthorizerPool(authorizers);
    }

    @Test
    public void matching_evaluators_should_be_found() {
        AuthorizerPool authorizerPool = createEvaluatorPool(true, true, false);
        assertEquals(Evaluators.STRING_AUTHORIZER, authorizerPool.getAuthorizer(String.class));
        assertEquals(Evaluators.INTEGER_AUTHORIZER, authorizerPool.getAuthorizer(Integer.class));
    }

    @Test
    public void inheritance_matching_should_work() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Evaluators.FOO_AUTHORIZER);

        AuthorizerPool authorizerPool = new AuthorizerPool(authorizers);

        Authorizer<Evaluators.Foo> fooAuthorizer = authorizerPool.getAuthorizer(Evaluators.Foo.class);

        assertNotNull(fooAuthorizer);
        assertFalse(fooAuthorizer.isGranted(new Evaluators.Bar()));
        assertFalse(fooAuthorizer.isGranted(new Evaluators.Foo()));
        Authorizer<Evaluators.Bar> barAuthorizer = authorizerPool.getAuthorizer(Evaluators.Bar.class);
        assertNotNull(barAuthorizer);
        assertFalse(barAuthorizer.isGranted(new Evaluators.Bar()));
    }

    @Test
    public void conflicting_evaluators_should_return_best_fit() {
        AuthorizerPool authorizerPool = createEvaluatorPool(true, true, true);
        final Authorizer<String> authorizer = authorizerPool.getAuthorizer(String.class);
        assertEquals(Evaluators.STRING_AUTHORIZER, authorizer);
        authorizer.isGranted("");
    }

    @Test
    public void no_matching_evaluator_should_return_best_fit() {
        AuthorizerPool authorizerPool = createEvaluatorPool(true, false, true);
        final Authorizer<String> authorizer = authorizerPool.getAuthorizer(String.class);
        assertNotNull(authorizer);
        authorizer.isGranted("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void no_matching_evaluators_should_throw_illegal_argument() {
        AuthorizerPool authorizerPool = createEvaluatorPool(true, true, false);
        authorizerPool.getAuthorizer(Float.class);
    }

}
