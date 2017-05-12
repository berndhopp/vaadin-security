package org.ilay;

import org.ilay.api.Restrict;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
        final Optional<CurrentRestrict> currentRestrictOptional = VaadinAbstraction.getFromSessionStore(CurrentRestrict.class);

        if (currentRestrictOptional.isPresent()) {
            final CurrentRestrict currentRestrict = currentRestrictOptional.get();

            currentRestrict.setRestrict(restrict);
        } else {
            CurrentRestrict currentRestrict = new CurrentRestrict();
            currentRestrict.setRestrict(restrict);
            VaadinAbstraction.storeInSession(CurrentRestrict.class, currentRestrict);
        }
    }

    static void currentRestrictIs(Restrict restrict) {
        requireNonNull(restrict);

        final Optional<CurrentRestrict> currentRestrictOptional = VaadinAbstraction.getFromSessionStore(CurrentRestrict.class);

        state(currentRestrictOptional.isPresent());

        final CurrentRestrict currentRestrict = currentRestrictOptional.get();

        state(currentRestrict.getRestrict() == restrict);
    }

    static void noUnclosedRestrict() {
        final Optional<CurrentRestrict> currentRestrictOptional = VaadinAbstraction.getFromSessionStore(CurrentRestrict.class);

        if (!currentRestrictOptional.isPresent()) {
            return;
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        CurrentRestrict currentRestrict = currentRestrictOptional.get();

        if (currentRestrict.getRestrict() != null) {

            final Restrict restrict = currentRestrict.getRestrict();

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

    static <T> void arraySanity(T[] array) {
        requireNonNull(array, "array must not be null");
        arg(array.length > 0, "array must not be empty");

        for (int i = 0; i < array.length; i++) {
            T x = array[i];
            requireNonNull(x, "elements in array must not be null");

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
                T y = array[j];

                arg(!x.equals(y), "duplicate entries found in array");
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

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalGetWithoutIsPresent"})
    static <T> T present(Optional<T> optional) {
        requireNonNull(optional);

        state(optional.isPresent());

        return optional.get();
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
