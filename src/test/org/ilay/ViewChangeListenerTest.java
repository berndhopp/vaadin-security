package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ServiceException;

import org.ilay.api.Authorizer;
import org.ilay.api.InMemoryAuthorizer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.ilay.Authorization.restrictView;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ViewChangeListenerTest {

    User user;

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

        assertTrue(authorizationContext.isViewAuthorized(myView));
    }

    @Test
    public void test_single_permission_should_work() throws ServiceException {
        View myView = e -> {
        };

        user.getRoles().clear();
        user.getRoles().add("user");

        restrictView(myView).to("admin");

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        assertFalse(authorizationContext.isViewAuthorized(myView));

        user.getRoles().add("admin");

        assertTrue(authorizationContext.isViewAuthorized(myView));
    }

    @Test
    public void view_change_should_invoke_isViewAuthorized(){
        AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        authorizationContext = spy(authorizationContext);

        ViewChangeListener.ViewChangeEvent viewChangeEvent = mock(ViewChangeListener.ViewChangeEvent.class);

        View view = e -> {};

        when(viewChangeEvent.getNewView()).thenReturn(view);

        authorizationContext.beforeViewChange(viewChangeEvent);

        verify(authorizationContext, times(1)).isViewAuthorized(view);
    }

    @Test
    public void test_multiple_permissions_should_work() throws ServiceException {
        user.setClearance(Clearance.NON);

        View myView = e -> {
        };

        user.getRoles().add("user");

        restrictView(myView).to(Clearance.TOP_SECRET, "admin", "user");

        final AuthorizationContext authorizationContext = AuthorizationContext.getCurrent();

        assertFalse(authorizationContext.isViewAuthorized(myView));

        user.getRoles().add("admin");

        assertFalse(authorizationContext.isViewAuthorized(myView));

        user.setClearance(Clearance.SECRET);

        assertFalse(authorizationContext.isViewAuthorized(myView));

        user.setClearance(Clearance.TOP_SECRET);

        assertTrue(authorizationContext.isViewAuthorized(myView));

        user.getRoles().remove("admin");

        assertFalse(authorizationContext.isViewAuthorized(myView));
    }
}
