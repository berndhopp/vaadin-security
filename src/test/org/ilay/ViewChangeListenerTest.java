package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ServiceException;

import org.ilay.api.Authorizer;
import org.ilay.api.InMemoryAuthorizer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.ilay.Authorization.restrictView;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ViewChangeListenerTest {

    private User user;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException, ServiceException {
        TestUtil.beforeTest();

        user = new User();

        InMemoryAuthorizer<String> roleAuthorizer = new InMemoryAuthorizer<String>() {
            @Override
            public boolean isGranted(String s) {
                return user.getRoles().contains(s);
            }

            @Override
            public Class<String> getPermissionClass() {
                return String.class;
            }
        };

        InMemoryAuthorizer<Clearance> clearanceAuthorizer = new InMemoryAuthorizer<Clearance>() {
            @Override
            public boolean isGranted(Clearance clearance) {
                return user.getClearance().ordinal() >= clearance.ordinal();
            }

            @Override
            public Class<Clearance> getPermissionClass() {
                return Clearance.class;
            }
        };

        Set<Authorizer> authorizers = new HashSet<>();
        authorizers.add(roleAuthorizer);
        authorizers.add(clearanceAuthorizer);

        Authorization.start(authorizers);

        ((TestUtil.TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();
    }

    @Test
    public void test_no_permission_should_work() throws ServiceException {
        user.setClearance(Clearance.NON);

        View myView = e -> {
        };

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        ViewChangeListener.ViewChangeEvent viewChangeEvent = mock(ViewChangeListener.ViewChangeEvent.class);

        when(viewChangeEvent.getNewView()).thenReturn(myView);

        assertTrue(authorizationContext.beforeViewChange(viewChangeEvent));
    }

    @Test
    public void test_single_permission_should_work() throws ServiceException {
        View myView = e -> {
        };

        user.getRoles().clear();
        user.getRoles().add("user");

        restrictView(myView).to("admin");

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        ViewChangeListener.ViewChangeEvent viewChangeEvent = mock(ViewChangeListener.ViewChangeEvent.class);

        when(viewChangeEvent.getNewView()).thenReturn(myView);

        assertFalse(authorizationContext.beforeViewChange(viewChangeEvent));

        user.getRoles().add("admin");

        assertTrue(authorizationContext.beforeViewChange(viewChangeEvent));
    }

    @Test
    public void test_multiple_permissions_should_work() throws ServiceException {
        user.setClearance(Clearance.NON);

        View myView = e -> {
        };

        user.getRoles().add("user");

        restrictView(myView).to(Clearance.TOP_SECRET, "admin", "user");

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        ViewChangeListener.ViewChangeEvent viewChangeEvent = mock(ViewChangeListener.ViewChangeEvent.class);

        when(viewChangeEvent.getNewView()).thenReturn(myView);

        assertFalse(authorizationContext.beforeViewChange(viewChangeEvent));

        user.getRoles().add("admin");

        assertFalse(authorizationContext.beforeViewChange(viewChangeEvent));

        user.setClearance(Clearance.SECRET);

        assertFalse(authorizationContext.beforeViewChange(viewChangeEvent));

        user.setClearance(Clearance.TOP_SECRET);

        assertTrue(authorizationContext.beforeViewChange(viewChangeEvent));

        user.getRoles().remove("admin");

        assertFalse(authorizationContext.beforeViewChange(viewChangeEvent));
    }
}
