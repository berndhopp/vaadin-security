package org.ilay;

import java.util.Collection;
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

    static void openBindIs(Authorization.HasSet bind) {
        requireNonNull(bind);

        final Optional<Authorization.HasSet> current = OpenBind.getCurrent();
        state(current.isPresent());
        state(current.get() == bind);
    }

    static void noOpenBind() {
        final Optional<Authorization.HasSet> currentOptional = OpenBind.getCurrent();

        if (currentOptional.isPresent()) {
            Authorization.HasSet current = currentOptional.get();

            if (current instanceof ComponentBind) {
                throw new IllegalStateException("Authorization.bindComponent() or Authorization.bindComponents() has been called without calling to() on the returned Bind-object");
            } else if (current instanceof ComponentUnbind) {
                throw new IllegalStateException("Authorization.unbindComponent() or Authorization.unbindComponents() has been called without calling from() on the returned Bind-object");
            } else if (current instanceof ViewBind) {
                throw new IllegalStateException("Authorization.bindView() or Authorization.bindViews() has been called without calling to() on the returned Bind-object");
            } else if (current instanceof ViewUnbind) {
                throw new IllegalStateException("Authorization.unbindView() or Authorization.unbindViews() has been called without calling from() on the returned Bind-object");
            } else {
                //will never come here
                throw new IllegalStateException("unknown Bind");
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
