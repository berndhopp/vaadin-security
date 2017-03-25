package org.ilay;

import org.ilay.api.Authorizer;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class AuthorizerPoolTest {

    static AuthorizerPool createEvaluatorPool(boolean withInt, boolean withString, boolean withObject) {
        Set<Authorizer> authorizers = new HashSet<>();

        if (withInt) {
            authorizers.add(Authorizers.INTEGER_AUTHORIZER);
        }

        if (withString) {
            authorizers.add(Authorizers.STRING_AUTHORIZER);
        }

        if (withObject) {
            authorizers.add(Authorizers.OBJECT_AUTHORIZER);
        }

        return new AuthorizerPool(authorizers);
    }

    @Test
    public void matching_evaluators_should_be_found() {
        AuthorizerPool authorizerPool = createEvaluatorPool(true, true, false);
        assertEquals(Authorizers.STRING_AUTHORIZER, authorizerPool.getAuthorizer(String.class));
        assertEquals(Authorizers.INTEGER_AUTHORIZER, authorizerPool.getAuthorizer(Integer.class));
    }

    @Test
    public void inheritance_matching_should_work() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.FOO_AUTHORIZER);

        AuthorizerPool authorizerPool = new AuthorizerPool(authorizers);

        Authorizer<Foo, ?> fooAuthorizer = authorizerPool.getAuthorizer(Foo.class);

        assertNotNull(fooAuthorizer);
        assertFalse(fooAuthorizer.isGranted(new Bar()));
        assertFalse(fooAuthorizer.isGranted(new Foo()));
        Authorizer<Bar, ?> barAuthorizer = authorizerPool.getAuthorizer(Bar.class);
        assertNotNull(barAuthorizer);
        assertFalse(barAuthorizer.isGranted(new Bar()));
    }

    @Test
    public void rivaling_evaluators_should_return_best_fit() {
        AuthorizerPool authorizerPool = createEvaluatorPool(true, true, true);
        final Authorizer<String, ?> authorizer = authorizerPool.getAuthorizer(String.class);
        assertEquals(Authorizers.STRING_AUTHORIZER, authorizer);
        authorizer.isGranted("");
    }

    @Test(expected = ConflictingEvaluatorsException.class)
    public void conflicting_evaluators_should_throw_exception() {
        List<Authorizer> authorizerList = new ArrayList<>();
        authorizerList.add(Authorizers.FOO_AUTHORIZER);
        authorizerList.add(Authorizers.FOO_AUTHORIZER);

        new AuthorizerPool(authorizerList);
    }

    @Test(expected = ConflictingEvaluatorsException.class)
    public void undecideable_interfaces_should_throw_exception() {
        List<Authorizer> authorizerList = new ArrayList<>();
        authorizerList.add(Authorizers.FOO_AUTHORIZER);
        authorizerList.add(Authorizers.BAR_AUTHORIZER);

        final AuthorizerPool authorizerPool = new AuthorizerPool(authorizerList);

        authorizerPool.getAuthorizer(Serializable.class);
    }

    @Test
    public void decideable_interfaces_should_work() {
        List<Authorizer> authorizerList = new ArrayList<>();
        authorizerList.add(Authorizers.FOO_AUTHORIZER);

        final AuthorizerPool authorizerPool = new AuthorizerPool(authorizerList);

        final Authorizer<Serializable, ?> authorizer = authorizerPool.getAuthorizer(Serializable.class);

        authorizer.isGranted(new Foo());
    }

    @Test
    public void no_matching_evaluator_should_return_best_fit() {
        AuthorizerPool authorizerPool = createEvaluatorPool(true, false, true);
        final Authorizer<String, ?> authorizer = authorizerPool.getAuthorizer(String.class);
        assertNotNull(authorizer);
        authorizer.isGranted("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void no_matching_evaluators_should_throw_illegal_argument() {
        AuthorizerPool authorizerPool = createEvaluatorPool(true, true, false);
        authorizerPool.getAuthorizer(Float.class);
    }

}
