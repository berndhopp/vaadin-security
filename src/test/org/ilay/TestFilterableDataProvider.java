package org.ilay;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.shared.Registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

class TestFilterableDataProvider implements DataProvider<TestFilterableDataProvider.Document, TestFilterableDataProvider.UserId> {

    private final Collection<Document> documents = new ArrayList<>();

    TestFilterableDataProvider() {
        documents.add(new Document(new UserId(1)));
        documents.add(new Document(new UserId(1)));
        documents.add(new Document(new UserId(1)));
        documents.add(new Document(new UserId(1)));
        documents.add(new Document(new UserId(1)));
        documents.add(new Document(new UserId(2)));
        documents.add(new Document(new UserId(2)));
        documents.add(new Document(new UserId(2)));
        documents.add(new Document(new UserId(2)));
        documents.add(new Document(new UserId(2)));
        documents.add(new Document(new UserId(3)));
        documents.add(new Document(new UserId(3)));
        documents.add(new Document(new UserId(3)));
        documents.add(new Document(new UserId(3)));
        documents.add(new Document(new UserId(3)));
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<Document, UserId> query) {
        Optional<UserId> userIdOptional = query.getFilter();

        //noinspection OptionalIsPresent
        if (userIdOptional.isPresent()) {
            return (int) documents
                    .stream()
                    .filter(doc -> doc.getUserId().equals(userIdOptional.get()))
                    .count();
        }

        return documents.size();
    }

    @Override
    public Stream<Document> fetch(Query<Document, UserId> query) {
        Optional<UserId> userIdOptional = query.getFilter();

        Stream<Document> stream = documents.stream();

        if (userIdOptional.isPresent()) {
            stream = stream.filter(doc -> doc.getUserId().equals(userIdOptional.get()));
        }

        return stream;
    }

    @Override
    public void refreshItem(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<Document> dataProviderListener) {
        throw new UnsupportedOperationException();
    }

    static class Document {
        private final UserId userId;

        Document(UserId userId) {
            this.userId = userId;
        }

        UserId getUserId() {
            return userId;
        }
    }

    static class UserId {
        private final int id;

        UserId(int id) {
            this.id = id;
        }

        int getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof UserId)) {
                return false;
            }

            UserId otherUserId = (UserId) obj;

            return getId() == otherUserId.getId();
        }
    }
}
