package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ServiceException;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import org.ilay.api.Authorizer;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.ilay.Authorization.restrictComponent;
import static org.ilay.Authorization.restrictComponents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class RestrictTest {

    @Before
    public void setup() throws ServiceException {
        TestUtil.beforeTest();
    }

    @Test(expected = IllegalStateException.class)
    public void unclosed_component_bind_should_throw_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        TestUtil.newSession();

        Button button = new Button();
        Button button2 = new Button();

        Authorization.restrictComponent(button);
        Authorization.restrictComponent(button2);
    }

    @Test(expected = IllegalStateException.class)
    public void unclosed_components_bind_should_throw_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        TestUtil.newSession();

        Button button = new Button();
        Button button2 = new Button();

        restrictComponents(button, button2);
        restrictComponents(button2, button);
    }

    @Test(expected = IllegalStateException.class)
    public void call_revert_twice_should_throw_exception() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        TestUtil.newSession();

        Button button = new Button();
        Button button2 = new Button();

        final Registration registration = restrictComponents(button, button2).to("whatever");

        registration.remove();
        registration.remove();
    }

    @Test(expected = IllegalStateException.class)
    public void unclosed_view_bind_should_throw_exception() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        TestUtil.newSession();

        View view = new View() {
            @Override
            public void enter(ViewChangeListener.ViewChangeEvent event) {

            }
        };
        View view2 = new View() {
            @Override
            public void enter(ViewChangeListener.ViewChangeEvent event) {

            }
        };

        Authorization.restrictView(view);
        Authorization.restrictView(view2);
    }

    @Test(expected = IllegalStateException.class)
    public void unclosed_views_bind_should_throw_exception() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        TestUtil.newSession();

        View view = new View() {
            @Override
            public void enter(ViewChangeListener.ViewChangeEvent event) {

            }
        };

        View view2 = new View() {
            @Override
            public void enter(ViewChangeListener.ViewChangeEvent event) {

            }
        };

        Authorization.restrictViews(view, view2);
        Authorization.restrictViews(view2, view);
    }

    @Test(expected = IllegalArgumentException.class)
    public void component_bind_empty_throws_illegal_argument_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        restrictComponents();
    }

    @Test(expected = IllegalArgumentException.class)
    public void component_bind_to_empty_permissions_throws_illegal_argument_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        TestUtil.newSession();

        restrictComponents(new Button()).to();
    }

    @Test(expected = IllegalArgumentException.class)
    public void view_bind_empty_throws_illegal_argument_exception() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        TestUtil.newSession();

        Authorization.restrictViews();
    }

    @Test(expected = IllegalArgumentException.class)
    public void view_bind_to_empty_permissions_throws_illegal_argument_exception() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        TestUtil.newSession();

        Authorization.restrictViews(new View() {
            @Override
            public void enter(ViewChangeListener.ViewChangeEvent event) {
            }
        }).to();
    }


    @Test
    public void test_components() throws ServiceException {

        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        TestUtil.newSession();

        Component component = new Button();
        Component component2 = new Button();

        restrictComponents(component, component2).to("hello", "world", 23);
        restrictComponent(component).to("foo");
        restrictComponent(component2).to("bar");

        AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        final Map<Component, Set<Object>> componentsToPermissions = authorizationContext.getComponentsToPermissions();

        //check that permissions of component1 are as expected
        Collection<Object> permissions1 = componentsToPermissions.get(component);

        assertNotNull(permissions1);
        assertEquals(4, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("hello", "world", 23, "foo"));

        //check that permissions of component2 are as expected
        Collection<Object> permissions2 = componentsToPermissions.get(component2);

        assertNotNull(permissions2);
        assertEquals(4, permissions2.size());
        assertThat(permissions2, containsInAnyOrder("hello", "world", 23, "bar"));

        //add one permission to component 1 and check that it is there
        final Registration registration = restrictComponent(component).to(42);

        permissions1 = componentsToPermissions.get(component);

        assertNotNull(permissions1);
        assertEquals(5, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("hello", "world", 23, "foo", 42));

        //remove one permissions from component1 and check it is gone

        registration.remove();

        permissions1 = componentsToPermissions.get(component);

        assertNotNull(permissions1);
        assertEquals(4, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("hello", "world", 23, "foo"));
    }

    @Test
    public void test_views() throws ServiceException {

        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        View view = new View() {
            @Override
            public void enter(ViewChangeListener.ViewChangeEvent event) {
            }
        };

        View view2 = new View() {
            @Override
            public void enter(ViewChangeListener.ViewChangeEvent event) {
            }
        };

        Authorization.start(authorizers);

        TestUtil.newSession();

        Authorization.restrictViews(view, view2).to("hello", "world", 23);
        Authorization.restrictView(view).to("foo");
        Authorization.restrictView(view2).to("bar");

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        final Map<View, Set<Object>> viewsToPermissions = authorizationContext.getViewsToPermissions();

        //check that permissions of view1 are as expected
        Collection<Object> permissions1 = viewsToPermissions.get(view);

        assertNotNull(permissions1);
        assertEquals(4, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("hello", "world", 23, "foo"));

        //check that permissions of view2 are as expected
        Collection<Object> permissions2 = viewsToPermissions.get(view2);

        assertNotNull(permissions2);
        assertEquals(4, permissions2.size());
        assertThat(permissions2, containsInAnyOrder("hello", "world", 23, "bar"));

        //add one permission to view 1 and check that it is there
        final Registration registration = Authorization.restrictView(view).to(42);

        permissions1 = viewsToPermissions.get(view);

        assertNotNull(permissions1);
        assertEquals(5, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("hello", "world", 23, "foo", 42));

        //remove some permissions from view1 and check they are gone

        registration.remove();

        permissions1 = viewsToPermissions.get(view);

        assertNotNull(permissions1);
        assertEquals(4, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("hello", "world", 23, "foo"));
    }
}