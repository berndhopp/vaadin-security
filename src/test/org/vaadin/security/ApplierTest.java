package org.vaadin.security;

import com.google.common.collect.ImmutableSet;

import com.vaadin.ui.Button;

import org.junit.Test;
import org.vaadin.security.api.Evaluator;
import org.vaadin.security.api.EvaluatorPool;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApplierTest {

    @Test
    public void applier_full_test() {

        final User user = new User();

        user.getRoles().add("user");

        Evaluator<String> roleEvaluator = new Evaluator<String>() {
            @Override
            public boolean evaluate(String s) {
                return user.getRoles().contains(s);
            }

            @Override
            public Class<String> getPermissionClass() {
                return String.class;
            }
        };

        Evaluator<Clearance> clearanceEvaluator = new Evaluator<Clearance>() {
            @Override
            public boolean evaluate(Clearance clearance) {
                return user.getClearance().ordinal() >= clearance.ordinal();
            }

            @Override
            public Class<Clearance> getPermissionClass() {
                return Clearance.class;
            }
        };

        EvaluatorPool evaluatorPool = new DefaultEvaluatorPool(ImmutableSet.of(roleEvaluator, clearanceEvaluator));

        TestAuthorizationEngine authorizationEngine = new TestAuthorizationEngine(evaluatorPool);

        Button button1 = new Button();
        Button button2 = new Button();
        Button button3 = new Button();

        authorizationEngine
                .bindComponent(button1).to("user", Clearance.NON)
                .bindComponent(button2).to("user", Clearance.SECRET)
                .bindComponent(button3).to("admin", Clearance.TOP_SECRET);

        assertTrue(button1.isVisible());
        assertFalse(button2.isVisible());
        assertFalse(button3.isVisible());

        authorizationEngine.applyAll();

        assertTrue(button1.isVisible());
        assertFalse(button2.isVisible());
        assertFalse(button3.isVisible());

        user.setClearance(Clearance.SECRET);

        authorizationEngine.applyAll();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertFalse(button3.isVisible());

        user.setClearance(Clearance.TOP_SECRET);
        user.getRoles().add("admin");
        authorizationEngine.applyAll();

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertTrue(button3.isVisible());

        user.setClearance(Clearance.SECRET);

        assertTrue(button1.isVisible());
        assertTrue(button2.isVisible());
        assertTrue(button3.isVisible());

        authorizationEngine.applyAll();

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
