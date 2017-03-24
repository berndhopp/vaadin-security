package org.ilay;

import com.vaadin.server.ServiceException;
import com.vaadin.ui.Button;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class ApiTest {
    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        TestUtil.beforeTest();
    }

    @Test(expected = IllegalStateException.class)
    public void multiple_start_calls_throw_exception() {
        Set<Authorizer> authorizers = new HashSet<>();

        authorizers.add(new InMemoryAuthorizer<Foo>() {
            @Override
            public boolean isGranted(Foo permission) {
                return true;
            }

            @Override
            public Class<Foo> getPermissionClass() {
                return Foo.class;
            }
        });

        Authorization.start(authorizers);
        Authorization.start(authorizers);
    }

    @Test(expected = IllegalStateException.class)
    public void call_without_start_throws_exception() {
        Authorization.bindComponent(new Button()).to("");
    }

    @Test
    public void test_start_1() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();

        authorizers.add(new InMemoryAuthorizer<Foo>() {
            @Override
            public boolean isGranted(Foo permission) {
                return true;
            }

            @Override
            public Class<Foo> getPermissionClass() {
                return Foo.class;
            }
        });

        Authorization.start(authorizers);

        //urgh
        ((TestSessionInitNotifierSupplier) Authorization.sessionInitNotifierSupplier).newSession();

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        assertNotNull(authorizationContext);
    }

    @Test
    public void test_start_2() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();

        authorizers.add(new InMemoryAuthorizer<Foo>() {
            @Override
            public boolean isGranted(Foo permission) {
                return true;
            }

            @Override
            public Class<Foo> getPermissionClass() {
                return Foo.class;
            }
        });

        Authorization.start(() -> authorizers);

        //urgh
        ((TestSessionInitNotifierSupplier) Authorization.sessionInitNotifierSupplier).newSession();

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        assertNotNull(authorizationContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_start_negative() {
        Authorization.start(new HashSet<>());
    }

    @Test
    public void test_start_supplier() {
        Authorization.start(HashSet::new);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_start_supplier_negative() throws ServiceException {
        Authorization.start(HashSet::new);
        ((TestSessionInitNotifierSupplier) Authorization.sessionInitNotifierSupplier).newSession();
    }
}
