package org.vaadin.authorization;

import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.vaadin.authorization.Authorization.Evaluator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.vaadin.authorization.Authorization.bindComponent;
import static org.vaadin.authorization.Authorization.bindComponents;
import static org.vaadin.authorization.Authorization.bindView;
import static org.vaadin.authorization.Authorization.bindViews;

public class BinderTest {

    @Test
    public void test_components() {

        Set<Evaluator> evaluators = new HashSet<>();
        evaluators.add(Evaluators.stringEvaluator);
        evaluators.add(Evaluators.integerEvaluator);

        Component component = new Button();
        Component component2 = new Button();

        bindComponents(component, component2).to("hello", "world", 23);
        bindComponent(component).to("foo");
        bindComponent(component2).to("bar");

        AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        final Map<Component, Collection<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();

        //check that permissions of component1 are as expected
        Collection<Object> permissions1 = componentsToPermissions.get(component);

        assertNotNull(permissions1);
        assertEquals(4, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("hello", "world", 23, "foo"));

        //check that permissions of component2 are as expected
        Collection<Object> permissions2 = componentsToPermissions.get(component2);

        assertNotNull(permissions2);
        assertEquals(4, permissions2.size());
        assertThat(permissions2, Matchers.containsInAnyOrder("hello", "world", 23, "bar"));

        //add one permission to component 1 and check that it is there
        bindComponent(component).to(42);

        permissions1 = componentsToPermissions.get(component);

        assertNotNull(permissions1);
        assertEquals(5, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("hello", "world", 23, "foo", 42));

        //remove some permissions from component1 and check they are gone

        Authorization.unbindComponent(component).from("hello", 23);

        permissions1 = componentsToPermissions.get(component);

        assertNotNull(permissions1);
        assertEquals(3, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("world", "foo", 42));
    }

    @Test
    public void test_views() {

        Set<Evaluator> evaluators = new HashSet<>();
        evaluators.add(Evaluators.stringEvaluator);
        evaluators.add(Evaluators.integerEvaluator);

        View view = viewChangeEvent -> {
        };

        View view2 = viewChangeEvent -> {
        };

        bindViews(view, view2).to("hello", "world", 23);
        bindView(view).to("foo");
        bindView(view2).to("bar");

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        final Map<View, Collection<Object>> viewsToPermissions = authorizationContext.getViewsToPermissions();

        //check that permissions of view1 are as expected
        Collection<Object> permissions1 = viewsToPermissions.get(view);

        assertNotNull(permissions1);
        assertEquals(4, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("hello", "world", 23, "foo"));

        //check that permissions of view2 are as expected
        Collection<Object> permissions2 = viewsToPermissions.get(view2);

        assertNotNull(permissions2);
        assertEquals(4, permissions2.size());
        assertThat(permissions2, Matchers.containsInAnyOrder("hello", "world", 23, "bar"));

        //add one permission to view 1 and check that it is there
        bindView(view).to(42);

        permissions1 = viewsToPermissions.get(view);

        assertNotNull(permissions1);
        assertEquals(5, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("hello", "world", 23, "foo", 42));

        //remove some permissions from view1 and check they are gone

        Authorization.unbindView(view).from("hello", 23);

        permissions1 = viewsToPermissions.get(view);

        assertNotNull(permissions1);
        assertEquals(3, permissions1.size());
        assertThat(permissions1, Matchers.containsInAnyOrder("world", "foo", 42));
    }
}