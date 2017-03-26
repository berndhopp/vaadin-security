package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.server.ServiceException;
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
import static org.ilay.Authorization.bindComponent;
import static org.ilay.Authorization.unbindComponent;
import static org.ilay.Authorization.unbindComponents;
import static org.ilay.Authorization.unbindView;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class BindTest {

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        TestUtil.beforeTest();
    }

    @Test(expected = IllegalArgumentException.class)
    public void component_bind_empty_throws_illegal_argument_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        Authorization.bindComponents();
    }

    @Test(expected = IllegalArgumentException.class)
    public void component_unbind_empty_throws_illegal_argument_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        Authorization.unbindComponents();
    }

    @Test(expected = IllegalArgumentException.class)
    public void component_unbind_to_empty_permissions_throws_illegal_argument_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        Authorization.unbindComponents(new Button()).from();
    }

    @Test(expected = IllegalArgumentException.class)
    public void component_bind_to_empty_permissions_throws_illegal_argument_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        Authorization.bindComponents(new Button()).to();
    }

    @Test(expected = IllegalArgumentException.class)
    public void view_bind_empty_throws_illegal_argument_exception() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        ((TestSessionInitNotifierSupplier) Authorization.sessionInitNotifierSupplier).newSession();

        Authorization.bindViews();
    }

    @Test(expected = IllegalArgumentException.class)
    public void view_unbind_empty_throws_illegal_argument_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        Authorization.unbindViews();
    }

    @Test(expected = IllegalArgumentException.class)
    public void view_unbind_to_empty_permissions_throws_illegal_argument_exception() {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        Authorization.unbindViews(e -> {
        }).from();
    }

    @Test(expected = IllegalArgumentException.class)
    public void view_bind_to_empty_permissions_throws_illegal_argument_exception() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);

        ((TestSessionInitNotifierSupplier) Authorization.sessionInitNotifierSupplier).newSession();

        Authorization.bindViews(e -> {
        }).to();
    }


    @Test
    public void test_components() throws ServiceException {

        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        Authorization.start(authorizers);


        ((TestSessionInitNotifierSupplier)Authorization.sessionInitNotifierSupplier).newSession();

        Component component = new Button();
        Component component2 = new Button();

        Authorization.bindComponents(component, component2).to("hello", "world", 23);
        bindComponent(component).to("foo");
        bindComponent(component2).to("bar");

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
        bindComponent(component).to(42);

        permissions1 = componentsToPermissions.get(component);

        assertNotNull(permissions1);
        assertEquals(5, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("hello", "world", 23, "foo", 42));

        //remove some permissions from component1 and check they are gone

        unbindComponent(component).from("hello", 23);

        permissions1 = componentsToPermissions.get(component);

        assertNotNull(permissions1);
        assertEquals(3, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("world", "foo", 42));

        unbindComponents(component).from("world");

        permissions1 = componentsToPermissions.get(component);

        assertNotNull(permissions1);
        assertEquals(2, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("foo", 42));

        unbindComponent(component).fromAll();

        permissions1 = componentsToPermissions.get(component);

        assertNull(permissions1);
    }

    @Test
    public void test_views() throws ServiceException {

        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(Authorizers.STRING_AUTHORIZER);
        authorizers.add(Authorizers.INTEGER_AUTHORIZER);

        View view = viewChangeEvent -> {
        };

        View view2 = viewChangeEvent -> {
        };

        Authorization.start(authorizers);


        ((TestSessionInitNotifierSupplier)Authorization.sessionInitNotifierSupplier).newSession();

        Authorization.bindViews(view, view2).to("hello", "world", 23);
        Authorization.bindView(view).to("foo");
        Authorization.bindView(view2).to("bar");

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
        Authorization.bindView(view).to(42);

        permissions1 = viewsToPermissions.get(view);

        assertNotNull(permissions1);
        assertEquals(5, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("hello", "world", 23, "foo", 42));

        //remove some permissions from view1 and check they are gone

        Authorization.unbindView(view).from("hello", 23);

        permissions1 = viewsToPermissions.get(view);

        assertNotNull(permissions1);
        assertEquals(3, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("world", "foo", 42));

        unbindView(view).from("world");

        permissions1 = viewsToPermissions.get(view);

        assertNotNull(permissions1);
        assertEquals(2, permissions1.size());
        assertThat(permissions1, containsInAnyOrder("foo", 42));

        unbindView(view).fromAll();

        permissions1 = viewsToPermissions.get(view);

        assertNull(permissions1);
    }
}