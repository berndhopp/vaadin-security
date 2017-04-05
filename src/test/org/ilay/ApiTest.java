package org.ilay;

import com.vaadin.server.ServiceException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;

import org.ilay.api.Authorizer;
import org.ilay.api.InMemoryAuthorizer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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

    @Test
    public void call_without_start_throws_exception() {

        try {
            Authorization.restrictComponent(new Button()).to("");
            fail();
        } catch (IllegalStateException e) {
        }

        try {
            Authorization.restrictComponents(new Button(), new Button()).to("");
            fail();
        } catch (IllegalStateException e) {
        }

        try {
            Authorization.restrictView(e -> {
            }).to("");
            fail();
        } catch (IllegalStateException e) {
        }

        try {
            Authorization.restrictViews(e -> {
            }, e -> {
            }).to("");
            fail();
        } catch (IllegalStateException e) {
        }

        try {
            Authorization.restrictData(Foo.class, new Grid<>(Foo.class));
            fail();
        } catch (IllegalStateException e) {
        }

        try {
            Authorization.reapplyRestrictions();
            fail();
        } catch (IllegalStateException e) {
        }
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

        ((TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

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

        ((TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

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
        ((TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();
    }
}
