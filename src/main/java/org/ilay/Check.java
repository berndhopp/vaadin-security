package org.ilay;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

class Check {

    static <T extends Collection> T notNullOrEmpty(T collection) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException("collection must not be null or empty");
        }

        return collection;
    }

    static <T> T[] arraySanity(T[] array) {
        requireNonNull(array, "array cannot be null");
        arg(array.length > 0, "array must nor be empty");

        for (T t : array) {
            requireNonNull(t, "elements in array must not be null");
        }

        return array;
    }

    static String notNullOrEmpty(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("input String must not be null or empty");
        }

        return input;
    }

    static void arg(boolean condition, String message, Object... parameters) {
        if (!condition) {
            throw new IllegalArgumentException(format(message, parameters));
        }
    }

    static void state(boolean condition) {
        if (!condition) {
            throw new IllegalStateException();
        }
    }

    static void state(boolean condition, String message, Object... parameters) {
        if (!condition) {
            throw new IllegalStateException(format(message, parameters));
        }
    }
}
