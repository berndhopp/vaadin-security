package org.ilay;

import com.vaadin.server.VaadinSession;

import org.ilay.api.Restrict;

import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;


/**
 * a set of precondition checks
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
final class Check {

    private Check() {
    }

    static <T extends Collection> void notNullOrEmpty(T collection) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException("collection must not be null or empty");
        }
    }

    static <T extends Map<U, V>, U, V> void notNullOrEmpty(T map) {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("map must not be null or empty");
        }
    }

    static void setCurrentRestrict(Restrict restrict) {
        final VaadinSession vaadinSession = notNull(VaadinSession.getCurrent());

        CurrentRestrict currentRestrict = vaadinSession.getAttribute(CurrentRestrict.class);

        if (currentRestrict == null) {
            currentRestrict = new CurrentRestrict();
            vaadinSession.setAttribute(CurrentRestrict.class, currentRestrict);
        }

        currentRestrict.setRestrict(restrict);
    }

    static void currentRestrictIs(Restrict restrict) {
        requireNonNull(restrict);

        final VaadinSession vaadinSession = notNull(VaadinSession.getCurrent());

        final CurrentRestrict currentRestrict = vaadinSession.getAttribute(CurrentRestrict.class);

        state(currentRestrict != null && currentRestrict.getRestrict() == restrict);
    }

    static void noUnclosedRestrict() {
        final VaadinSession vaadinSession = notNull(VaadinSession.getCurrent());

        final CurrentRestrict currentRestrict = vaadinSession.getAttribute(CurrentRestrict.class);

        if (currentRestrict == null) {
            return;
        }

        if (currentRestrict.getRestrict() != null) {

            final Restrict restrict = currentRestrict.getRestrict();

            if (restrict instanceof ComponentRestrictRegistration) {
                throw new IllegalStateException("Authorization.bindComponent() or Authorization.bindComponents() has been called without calling to() on the returned Restrict-object");
            } else if (restrict instanceof ViewRestrictRegistration) {
                throw new IllegalStateException("Authorization.bindView() or Authorization.bindViews() has been called without calling to() on the returned Restrict-object");
            } else {
                //will never come here
                throw new IllegalStateException("unknown Restrict");
            }
        }
    }

    static <T> void arraySanity(T[] array) {
        requireNonNull(array, "array must not be null");
        arg(array.length > 0, "array must not be empty");

        for (int i = 0; i < array.length; i++) {
            T x = requireNonNull(array[i], "elements in array must not be null");

            /*
               only the tuples marked with x need to be
               tested for equality

                  1 2 3 4
                1 - - - -
                2 x - - -
                3 x x - -
                4 x x x -
            */
            for (int j = i + 1; j < array.length; j++) {
                T y = requireNonNull(array[j], "elements in array must not be null");

                arg(!x.equals(y), "duplicate entries not allowed in array");
            }
        }
    }

    static void notNullOrEmpty(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("input String must not be null or empty");
        }
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

    static <T> T notNull(T value) {
        if (value == null) {
            throw new NullPointerException();
        }

        return value;
    }

    static <T> T notNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }

        return value;
    }

    private static class CurrentRestrict {
        private Restrict currentRestrict;

        Restrict getRestrict() {
            return currentRestrict;
        }

        void setRestrict(Restrict currentRestrict) {
            this.currentRestrict = currentRestrict;
        }
    }
}
