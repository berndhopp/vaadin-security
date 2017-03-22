package org.ilay;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

final class Util {

    private Util() {
    }

    static <T extends Collection> T checkNotEmpty(T collection) {
        requireNonNull(collection);

        if (collection.isEmpty()) {
            throw new IllegalArgumentException("evaluators cannot be empty");
        }

        return collection;
    }

    static void checkArg(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    static void checkArg(boolean condition, String message, Object... parameters) {
        if (!condition) {
            throw new IllegalArgumentException(format(message, parameters));
        }
    }
}


