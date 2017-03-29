package org.ilay;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.Arrays.asList;

final class CollectionUtil {

    private CollectionUtil() {
    }

    static <T> Set<T> toNonEmptySet(T[] array) {
        Objects.requireNonNull(array);
        Check.arg(array.length != 0, "array must not be empty");
        Set<T> set = new HashSet<>(array.length);
        Collections.addAll(set, array);
        return set;
    }

    static <T> Set<T> toNonEmptyCOWSet(T[] array) {
        Objects.requireNonNull(array);
        Check.arg(array.length != 0, "array must not be empty");
        return new CopyOnWriteArraySet<>(asList(array));
    }
}
