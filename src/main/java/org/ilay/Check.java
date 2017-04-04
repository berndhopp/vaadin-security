package org.ilay;

import com.vaadin.util.CurrentInstance;

import org.ilay.api.Restrict;

import java.util.Collection;
import java.util.Map;

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

    static void currentRestrictIs(Restrict restrict) {
        requireNonNull(restrict);

        final Restrict current = CurrentInstance.get(Restrict.class);
        state(current != null);
        state(current == restrict);
    }

    static void noRestrictOpen() {
        final Restrict restrict = CurrentInstance.get(Restrict.class);

        if (restrict != null) {

            if (restrict instanceof ComponentRestrict) {
                throw new IllegalStateException("Authorization.bindComponent() or Authorization.bindComponents() has been called without calling to() on the returned Restrict-object");
            } else if (restrict instanceof ViewRestrict) {
                throw new IllegalStateException("Authorization.bindView() or Authorization.bindViews() has been called without calling to() on the returned Restrict-object");
            } else {
                //will never come here
                throw new IllegalStateException("unknown Restrict");
            }
        }
    }

    static <T> T[] arraySanity(T[] array) {
        requireNonNull(array, "array must not be null");
        arg(array.length > 0, "array must not be empty");

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
