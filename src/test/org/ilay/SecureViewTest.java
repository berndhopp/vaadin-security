package org.ilay;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ServiceException;

import org.ilay.api.Authorizer;
import org.ilay.api.InMemoryAuthorizer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecureViewTest {

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        TestUtil.beforeTest();
    }

    @Test
    public void test_positive() throws ServiceException, SecureView.ParseException {
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

        ((TestUtil.TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

        SecureView<Foo> secureView = Mockito.spy(new FooSecureView(true));

        ViewChangeListener.ViewChangeEvent viewChangeEvent = Mockito.mock(ViewChangeListener.ViewChangeEvent.class);

        when(viewChangeEvent.getNewView()).thenReturn(secureView);

        when(viewChangeEvent.getParameters()).thenReturn("non empty parameters");

        AuthorizationContext.getCurrent().beforeViewChange(viewChangeEvent);

        secureView.enter(viewChangeEvent);

        verify(secureView, times(1)).parse("non empty parameters");
        verify(secureView, times(1)).enter(any(Foo.class));
    }

    @Test
    public void test_negative() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();

        authorizers.add(new InMemoryAuthorizer<Foo>() {
            @Override
            public boolean isGranted(Foo permission) {
                return false;
            }

            @Override
            public Class<Foo> getPermissionClass() {
                return Foo.class;
            }
        });

        Authorization.start(authorizers);

        ((TestUtil.TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

        SecureView<Foo> secureView = Mockito.spy(new FooSecureView(true));

        ViewChangeListener.ViewChangeEvent viewChangeEvent = Mockito.mock(ViewChangeListener.ViewChangeEvent.class);

        when(viewChangeEvent.getParameters()).thenReturn("non empty parameters");
        when(viewChangeEvent.getNewView()).thenReturn(secureView);

        AuthorizationContext.getCurrent().beforeViewChange(viewChangeEvent);

        verify(secureView, never()).enter(any(Foo.class));
    }

    @Test
    public void test_parseException() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();

        authorizers.add(new InMemoryAuthorizer<Foo>() {
            @Override
            public boolean isGranted(Foo permission) {
                return false;
            }

            @Override
            public Class<Foo> getPermissionClass() {
                return Foo.class;
            }
        });

        Authorization.start(authorizers);

        ((TestUtil.TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

        SecureView<Foo> secureView = Mockito.spy(new FooSecureView(false));

        ViewChangeListener.ViewChangeEvent viewChangeEvent = Mockito.mock(ViewChangeListener.ViewChangeEvent.class);

        when(viewChangeEvent.getParameters()).thenReturn("non empty parameters");
        when(viewChangeEvent.getNewView()).thenReturn(secureView);

        AuthorizationContext.getCurrent().beforeViewChange(viewChangeEvent);

        verify(secureView, never()).enter(any(Foo.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_parse() throws ServiceException {
        Set<Authorizer> authorizers = new HashSet<>();

        authorizers.add(new InMemoryAuthorizer<Foo>() {
            @Override
            public boolean isGranted(Foo permission) {
                return false;
            }

            @Override
            public Class<Foo> getPermissionClass() {
                return Foo.class;
            }
        });

        Authorization.start(authorizers);

        ((TestUtil.TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

        SecureView<Foo> secureView = new FooSecureView(false) {
            @Override
            protected Foo parse(String parameters) throws ParseException {
                return new Foo();
            }
        };

        ViewChangeListener.ViewChangeEvent viewChangeEvent = Mockito.mock(ViewChangeListener.ViewChangeEvent.class);

        when(viewChangeEvent.getNewView()).thenReturn(secureView);

        AuthorizationContext.getCurrent().beforeViewChange(viewChangeEvent);
    }
}
