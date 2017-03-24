package org.ilay;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

final class Check {

    private Check() {
    }

    static <T extends Collection> T notEmpty(T collection) {
        requireNonNull(collection);

        if (collection.isEmpty()) {
            throw new IllegalArgumentException("collection cannot be empty");
        }

        return collection;
    }

    static void that(boolean condition, String message, Object... parameters) {
        if (!condition) {
            throw new IllegalArgumentException(format(message, parameters));
        }
    }
}


