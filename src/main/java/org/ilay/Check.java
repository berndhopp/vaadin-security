package org.ilay;

import org.ilay.api.Restrict;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

class Check {

    static <T extends Collection> T notNullOrEmpty(T collection) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException("collection must not be null or empty");
        }

        return collection;
    }

    static <T extends Map<U, V>, U, V> T notNullOrEmpty(T map) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("map must not be null or empty");
        }

        return map;
    }

    static void openBindIs(Restrict bind) {
        requireNonNull(bind);

        final Optional<Restrict> current = OpenBind.getCurrent();
        state(current.isPresent());
        state(current.get() == bind);
    }

    static void noOpenBind() {
        final Optional<Restrict> currentOptional = OpenBind.getCurrent();

        if (currentOptional.isPresent()) {
            Restrict current = currentOptional.get();

            if (current instanceof ComponentRestrict) {
                throw new IllegalStateException("Authorization.bindComponent() or Authorization.bindComponents() has been called without calling to() on the returned Restrict-object");
            } else if (current instanceof ViewRestrict) {
                throw new IllegalStateException("Authorization.bindView() or Authorization.bindViews() has been called without calling to() on the returned Restrict-object");
            } else {
                //will never come here
                throw new IllegalStateException("unknown Restrict");
            }
        }
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
