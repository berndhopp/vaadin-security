package org.ilay;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ServiceException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AuthorizedViewTest {

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        TestUtil.beforeTest();
    }

    @Test
    public void test_positive() throws ServiceException {
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

        AuthorizedView<Foo> authorizedView = Mockito.spy(new FooAuthorizedView(true));

        ViewChangeListener.ViewChangeEvent viewChangeEvent = Mockito.mock(ViewChangeListener.ViewChangeEvent.class);

        authorizedView.enter(viewChangeEvent);

        verify(authorizedView, times(1)).onSuccessfulAuthorization(any());
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

        //urgh
        ((TestSessionInitNotifierSupplier) Authorization.sessionInitNotifierSupplier).newSession();

        AuthorizedView<Foo> authorizedView = Mockito.spy(new FooAuthorizedView(true));

        ViewChangeListener.ViewChangeEvent viewChangeEvent = Mockito.mock(ViewChangeListener.ViewChangeEvent.class);

        authorizedView.enter(viewChangeEvent);

        verify(authorizedView, times(1)).onFailedAuthorization(any());
    }

    @Test
    public void test_parseException() {
        AuthorizedView<Foo> authorizedView = Mockito.spy(new FooAuthorizedView(false));

        ViewChangeListener.ViewChangeEvent viewChangeEvent = Mockito.mock(ViewChangeListener.ViewChangeEvent.class);

        authorizedView.enter(viewChangeEvent);

        verify(authorizedView, times(1)).onParseException(any());
    }


    @Test(expected = NullPointerException.class)
    public void test_null_parse() {
        AuthorizedView<Foo> authorizedView = new FooAuthorizedView(false) {
            @Override
            protected Foo parse(String parameters) throws ParseException {
                return null;
            }
        };

        ViewChangeListener.ViewChangeEvent viewChangeEvent = Mockito.mock(ViewChangeListener.ViewChangeEvent.class);

        authorizedView.enter(viewChangeEvent);
    }


}
