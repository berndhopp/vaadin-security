package org.ilay;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

final class CollectionUtil {

    private CollectionUtil() {
    }

    static <T> Set<T> toSet(T[] array) {
        Objects.requireNonNull(array);
        return new HashSet<>(Arrays.asList(array));
    }

    static <T> Set<T> toSet(T t) {
        Objects.requireNonNull(t);
        return Collections.singleton(t);
    }
}
