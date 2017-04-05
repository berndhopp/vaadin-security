package org.ilay;

import com.vaadin.server.ServiceException;
import com.vaadin.ui.Button;

import org.ilay.api.Authorizer;
import org.ilay.api.InMemoryAuthorizer;
import org.ilay.api.Reverter;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ComponentsTest {

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        TestUtil.beforeTest();
    }

    @Test
    public void test_apply_all() throws ServiceException {

        final User user = new User();

        user.getRoles().add("user");

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

        ((TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

        Button button1 = new Button();
        Button button2 = new Button();
        Button button3 = new Button();

        Authorization.restrictComponent(button1).to("user", Clearance.NON);
        Authorization.restrictComponent(button2).to("user", Clearance.SECRET);
        final Reverter reverter = Authorization.restrictComponent(button3).to("admin", Clearance.TOP_SECRET);

        assertTrue(button1.isVisible());
        assertFalse(button2.isVisible());
        assertFalse(button3.isVisible());

        Authorization.reapplyRestrictions();

        assertTrue(button1.isVisible());
        assertFalse(button2.isVisible());
        assertFalse(button3.isVisible());

        user.setClearance(Clearance.SECRET);

        Authorization.reapplyRestrictions();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertFalse(button3.isVisible());

        user.setClearance(Clearance.TOP_SECRET);
        user.getRoles().add("admin");
        Authorization.reapplyRestrictions();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertTrue(button3.isVisible());

        user.setClearance(Clearance.SECRET);

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertTrue(button3.isVisible());

        Authorization.reapplyRestrictions();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertFalse(button3.isVisible());

        reverter.revert();

        Authorization.reapplyRestrictions();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertTrue(button3.isVisible());
    }

    @Test
    public void test_apply() throws ServiceException {

        final User user = new User();

        user.getRoles().add("user");

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

        ((TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

        Button button1 = new Button();
        Button button2 = new Button();
        Button button3 = new Button();

        Authorization.restrictComponent(button1).to("user", Clearance.NON);
        Authorization.restrictComponent(button2).to("user", Clearance.SECRET);
        Authorization.restrictComponent(button3).to("admin", Clearance.TOP_SECRET);

        assertTrue(button1.isVisible());
        assertFalse(button2.isVisible());
        assertFalse(button3.isVisible());

        Authorization.reapplyRestrictions();

        assertTrue(button1.isVisible());
        assertFalse(button2.isVisible());
        assertFalse(button3.isVisible());

        user.setClearance(Clearance.SECRET);

        Authorization.reapplyRestrictions();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertFalse(button3.isVisible());

        user.setClearance(Clearance.TOP_SECRET);
        user.getRoles().add("admin");
        Authorization.reapplyRestrictions();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertTrue(button3.isVisible());

        user.setClearance(Clearance.SECRET);

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertTrue(button3.isVisible());

        Authorization.reapplyRestrictions();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertFalse(button3.isVisible());
    }

    @Test(expected = IllegalStateException.class)
    public void test_manipulating_visibility_throws_illegal_state_exception() throws ServiceException {

        final User user = new User();

        user.getRoles().add("user");

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

        ((TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

        Button button1 = new Button();
        Button button2 = new Button();
        Button button3 = new Button();

        Authorization.restrictComponent(button1).to("user", Clearance.NON);
        Authorization.restrictComponent(button2).to("user", Clearance.SECRET);
        Authorization.restrictComponent(button3).to("admin", Clearance.TOP_SECRET);

        assertTrue(button1.isVisible());
        assertFalse(button2.isVisible());
        assertFalse(button3.isVisible());

        button1.setVisible(false);

        Authorization.reapplyRestrictions();
    }
}
