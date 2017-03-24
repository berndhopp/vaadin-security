package org.ilay;

import com.vaadin.server.ServiceException;
import com.vaadin.ui.Button;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApplyTest {

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        TestUtil.beforeTest();
    }

    @Test
    public void applier_full_test() throws ServiceException {

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

        //urgh
        ((TestSessionInitNotifierSupplier)Authorization.sessionInitNotifierSupplier).newSession();

        Button button1 = new Button();
        Button button2 = new Button();
        Button button3 = new Button();

        Authorization.bindComponent(button1).to("user", Clearance.NON);
        Authorization.bindComponent(button2).to("user", Clearance.SECRET);
        Authorization.bindComponent(button3).to("admin", Clearance.TOP_SECRET);

        assertTrue(button1.isVisible());
        assertFalse(button2.isVisible());
        assertFalse(button3.isVisible());

        Authorization.applyAll();

        assertTrue(button1.isVisible());
        assertFalse(button2.isVisible());
        assertFalse(button3.isVisible());

        user.setClearance(Clearance.SECRET);

        Authorization.applyAll();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertFalse(button3.isVisible());

        user.setClearance(Clearance.TOP_SECRET);
        user.getRoles().add("admin");
        Authorization.applyAll();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertTrue(button3.isVisible());

        user.setClearance(Clearance.SECRET);

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertTrue(button3.isVisible());

        Authorization.applyAll();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertFalse(button3.isVisible());
    }

    private enum Clearance {
        NON,
        SECRET,
        TOP_SECRET
    }

    private static class User {
        private final Set<String> roles = new HashSet<>();
        private Clearance clearance = Clearance.NON;

        Set<String> getRoles() {
            return roles;
        }

        Clearance getClearance() {
            return clearance;
        }

        void setClearance(Clearance clearance) {
            this.clearance = clearance;
        }
    }
}
