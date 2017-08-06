package org.ilay;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ServiceException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;

import org.ilay.api.Authorizer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

public class ApiTest {
    @Before
    public void setup() throws ServiceException {
        TestUtil.beforeTest();
    }

    @Test(expected = IllegalStateException.class)
    public void multiple_start_calls_throw_exception() {
        Set<Authorizer> authorizers = new HashSet<>();

        authorizers.add(new Authorizer<Foo>() {
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
            Authorization.restrictView(new View() {
                @Override
                public void enter(ViewChangeListener.ViewChangeEvent event) {

                }
            }).to("");
            fail();
        } catch (IllegalStateException e) {
        }

        try {
            Authorization.restrictViews(new View() {
                @Override
                public void enter(ViewChangeListener.ViewChangeEvent event) {

                }
            }, new View() {
                @Override
                public void enter(ViewChangeListener.ViewChangeEvent event) {

                }
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
        TestUtil.newSession();
    }
}
