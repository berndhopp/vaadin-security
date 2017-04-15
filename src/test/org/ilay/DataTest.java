package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.server.ServiceException;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;

import org.ilay.api.Authorizer;
import org.ilay.api.InMemoryAuthorizer;
import org.ilay.api.Reverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DataTest {

    private Foo foo1;
    private Foo foo2;
    private Foo foo3;
    private InMemoryAuthorizer<Foo> fooInMemoryAuthorizer;
    private InMemoryAuthorizer<String> stringInMemoryAuthorizer;
    private Authorizer<TestFilterableDataProvider.Document, TestFilterableDataProvider.UserId> userIdAuthorizer;
    private boolean userIdAuthorizerWorksCorrect = true;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException, ServiceException {
        TestUtil.beforeTest();

        Set<Authorizer> authorizers = new HashSet<>();

        foo1 = new Foo();

        foo2 = new Foo();

        foo3 = new Foo();

        fooInMemoryAuthorizer = new InMemoryAuthorizer<Foo>() {
            @Override
            public boolean isGranted(Foo permission) {
                return permission == foo1 || permission == foo2;
            }

            @Override
            public Class<Foo> getPermissionClass() {
                return Foo.class;
            }
        };
        authorizers.add(fooInMemoryAuthorizer);

        stringInMemoryAuthorizer = new InMemoryAuthorizer<String>() {
            @Override
            public boolean isGranted(String permission) {
                return "granted_string".equals(permission) ||
                       "another_granted_string".equals(permission);
            }

            @Override
            public Class<String> getPermissionClass() {
                return String.class;
            }
        };

        authorizers.add(stringInMemoryAuthorizer);

        userIdAuthorizer = new Authorizer<TestFilterableDataProvider.Document, TestFilterableDataProvider.UserId>() {

            private final TestFilterableDataProvider.UserId userId = new TestFilterableDataProvider.UserId(1);
            private final TestFilterableDataProvider.UserId userId2 = new TestFilterableDataProvider.UserId(2);

            @Override
            public boolean isGranted(TestFilterableDataProvider.Document permission) {
                return permission.getUserId().equals(userId);
            }

            @Override
            public Class<TestFilterableDataProvider.Document> getPermissionClass() {
                return TestFilterableDataProvider.Document.class;
            }

            @Override
            public TestFilterableDataProvider.UserId asFilter() {
                return userIdAuthorizerWorksCorrect ? userId : userId2;
            }
        };

        authorizers.add(userIdAuthorizer);

        Authorization.start(authorizers);

        ((TestUtil.TestSessionInitNotifierSupplier) VaadinAbstraction.getSessionInitNotifier()).newSession();

        userIdAuthorizerWorksCorrect = true;
    }

    @Test
    @SuppressWarnings({"unchecked", "RedundantCast", "Convert2Diamond"})
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

    @Test
    public void test_in_memory_filterable_positive(){
        ComboBox<String> comboBox = new ComboBox<>();

        comboBox.setItems("granted_string", "another_granted_string", "a_not_granted_string");

        Authorization.restrictData(String.class, comboBox);

        List<String> strings = comboBox.getDataProvider().fetch(new Query<>()).collect(toList());

        assertThat(strings, hasSize(2));
        assertThat(strings, hasItem("granted_string"));
        assertThat(strings, hasItem("another_granted_string"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_not_in_memory_positive(){
        TestFilterableDataProvider dataProvider = new TestFilterableDataProvider();

        Grid<TestFilterableDataProvider.Document> documentGrid = new Grid<>(TestFilterableDataProvider.Document.class);

        documentGrid.setDataProvider(dataProvider);

        Authorization.restrictData(TestFilterableDataProvider.Document.class, documentGrid);

        DataProvider<TestFilterableDataProvider.Document, TestFilterableDataProvider.UserId> documentGridDataProvider = (DataProvider<TestFilterableDataProvider.Document, TestFilterableDataProvider.UserId>) documentGrid.getDataProvider();

        Stream<TestFilterableDataProvider.Document> documentStream = documentGridDataProvider.fetch(new Query<>(new TestFilterableDataProvider.UserId(1)));

        List<TestFilterableDataProvider.Document> documentList = documentStream.collect(toList());

        assertThat(documentList, hasSize(5));

        for (TestFilterableDataProvider.Document document : documentList) {
            assertEquals(1, document.getUserId().getId());
        }
    }


    @Test(expected = IllegalStateException.class)
    @SuppressWarnings("unchecked")
    public void test_not_in_memory_negative(){
        TestFilterableDataProvider dataProvider = new TestFilterableDataProvider();

        Grid<TestFilterableDataProvider.Document> documentGrid = new Grid<>(TestFilterableDataProvider.Document.class);

        documentGrid.setDataProvider(dataProvider);

        Authorization.restrictData(TestFilterableDataProvider.Document.class, documentGrid);

        DataProvider<TestFilterableDataProvider.Document, TestFilterableDataProvider.UserId> documentGridDataProvider = (DataProvider<TestFilterableDataProvider.Document, TestFilterableDataProvider.UserId>) documentGrid.getDataProvider();

        userIdAuthorizerWorksCorrect = false;

        documentGridDataProvider.fetch(new Query<>(new TestFilterableDataProvider.UserId(1))).collect(toList());
    }
}
