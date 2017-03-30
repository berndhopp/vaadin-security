package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.server.ServiceException;
import com.vaadin.ui.Grid;

import org.ilay.api.Authorizer;
import org.ilay.api.InMemoryAuthorizer;
import org.ilay.api.Reverter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DataTest {

    private Foo foo1;
    private Foo foo2;
    private Foo foo3;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException, ServiceException {
        TestUtil.beforeTest();

        Set<Authorizer> authorizers = new HashSet<>();

        foo1 = new Foo();

        foo2 = new Foo();

        foo3 = new Foo();

        authorizers.add(new InMemoryAuthorizer<Foo>() {
            @Override
            public boolean isGranted(Foo permission) {
                return permission == foo1 || permission == foo2;
            }

            @Override
            public Class<Foo> getPermissionClass() {
                return Foo.class;
            }
        });

        Authorization.start(authorizers);


        ((TestSessionInitNotifierSupplier) Authorization.sessionInitNotifierSupplier).newSession();
    }

    @Test
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public void test_in_memory_positive() throws ServiceException {

        Grid<Foo> fooGrid = new Grid<>(Foo.class);

        fooGrid.setItems(foo1, foo2, foo3);

        final Reverter reverter = Authorization.restrictData(Foo.class, fooGrid);

        DataProvider<Foo, ?> dataProvider = fooGrid.getDataProvider();

        assertNotNull(dataProvider);

        List<Foo> items = (List<Foo>) dataProvider.fetch(new Query(0, 5, new ArrayList<QuerySortOrder>(), null, null)).collect(toList());

        assertThat(items, hasSize(2));
        assertThat(items, hasItem(foo1));
        assertThat(items, hasItem(foo2));
        assertThat(items, not(hasItem(foo3)));

        reverter.revert();

        dataProvider = fooGrid.getDataProvider();

        items = (List<Foo>) dataProvider.fetch(new Query(0, 5, new ArrayList<QuerySortOrder>(), null, null)).collect(toList());

        assertThat(items, hasSize(3));
        assertThat(items, hasItem(foo1));
        assertThat(items, hasItem(foo2));
        assertThat(items, hasItem(foo3));
    }
}
