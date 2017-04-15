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

    static class Document{
        private final int documentId;
        private final UserId userId;

        Document(int documentId, UserId userId) {
            this.documentId = documentId;
            this.userId = userId;
        }

        public int getDocumentId() {
            return documentId;
        }

        public UserId getUserId() {
            return userId;
        }
    }

    static class UserId{
        private final int id;

        UserId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof UserId)){
                return false;
            }

            UserId otherUserId = (UserId)obj;

            return getId() == otherUserId.getId();
        }
    }

    private final Collection<Document> documents = new ArrayList<>();

    TestFilterableDataProvider(){
        documents.add(new Document(1, new UserId(1)));
        documents.add(new Document(2, new UserId(1)));
        documents.add(new Document(3, new UserId(1)));
        documents.add(new Document(4, new UserId(1)));
        documents.add(new Document(5, new UserId(1)));
        documents.add(new Document(6, new UserId(2)));
        documents.add(new Document(7, new UserId(2)));
        documents.add(new Document(8, new UserId(2)));
        documents.add(new Document(9, new UserId(2)));
        documents.add(new Document(10, new UserId(2)));
        documents.add(new Document(11, new UserId(3)));
        documents.add(new Document(12, new UserId(3)));
        documents.add(new Document(13, new UserId(3)));
        documents.add(new Document(14, new UserId(3)));
        documents.add(new Document(15, new UserId(3)));
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<Document, UserId> query) {
        Optional<UserId> userIdOptional = query.getFilter();

        //noinspection OptionalIsPresent
        if(userIdOptional.isPresent()){
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
}
