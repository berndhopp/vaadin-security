package org.vaadin.security;

import com.google.common.collect.ImmutableSet;
import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.vaadin.security.impl.AuthorizationEngine;
import org.vaadin.security.impl.DefaultEvaluatorPool;
import org.vaadin.security.impl.TestAuthorizationEngine;

import java.util.Set;

import static org.junit.Assert.*;

public class BinderTest {

    @Test
    public void test_components() {

        AuthorizationEngine authorizationEngine = new TestAuthorizationEngine(
                new DefaultEvaluatorPool(
                        ImmutableSet.of(Evaluators.stringEvaluator, Evaluators.integerEvaluator)));

        Component component = new Button();
        Component component2 = new Button();

        authorizationEngine
                .bind(component, component2).to("hello", "world", 23)
                .bind(component).to("foo")
                .bind(component2).to("bar");

        //check that permissions of component1 are as expected
        Set<Object> permissions1 = authorizationEngine.getPermissions(component);

        assertNotNull(permissions1);
        assertEquals(4, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("hello", "world", 23, "foo"));

        //check that permissions of component2 are as expected
        Set<Object> permissions2 = authorizationEngine.getPermissions(component2);

        assertNotNull(permissions2);
        assertEquals(4, permissions2.size());
        assertThat(permissions2, Matchers.containsInAnyOrder("hello", "world", 23, "bar"));

        //add one permission to component 1 and check that it is there
        authorizationEngine.bind(component).to(42);

        permissions1 = authorizationEngine.getPermissions(component);

        assertNotNull(permissions1);
        assertEquals(5, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("hello", "world", 23, "foo", 42));

        //remove some permissions from component1 and check they are gone

        authorizationEngine.unbind(component).from("hello", 23);

        permissions1 = authorizationEngine.getPermissions(component);

        assertNotNull(permissions1);
        assertEquals(3, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("world", "foo", 42));
    }

    @Test
    public void test_views() {

        AuthorizationEngine authorizationEngine = new TestAuthorizationEngine(
                new DefaultEvaluatorPool(
                        ImmutableSet.of(Evaluators.stringEvaluator, Evaluators.integerEvaluator)));

        View view = viewChangeEvent -> {
        };

        View view2 = viewChangeEvent -> {
        };

        authorizationEngine
                .bindView(view, view2).to("hello", "world", 23)
                .bindView(view).to("foo")
                .bindView(view2).to("bar");

        //check that permissions of view1 are as expected
        Set<Object> permissions1 = authorizationEngine.getViewPermissions(view);

        assertNotNull(permissions1);
        assertEquals(4, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("hello", "world", 23, "foo"));

        //check that permissions of view2 are as expected
        Set<Object> permissions2 = authorizationEngine.getViewPermissions(view2);

        assertNotNull(permissions2);
        assertEquals(4, permissions2.size());
        assertThat(permissions2, Matchers.containsInAnyOrder("hello", "world", 23, "bar"));

        //add one permission to view 1 and check that it is there
        authorizationEngine.bindView(view).to(42);

        permissions1 = authorizationEngine.getViewPermissions(view);

        assertNotNull(permissions1);
        assertEquals(5, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("hello", "world", 23, "foo", 42));

        //remove some permissions from view1 and check they are gone

        authorizationEngine.unbindView(view).from("hello", 23);

        permissions1 = authorizationEngine.getViewPermissions(view);

        assertNotNull(permissions1);
        assertEquals(3, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("world", "foo", 42));
    }
}