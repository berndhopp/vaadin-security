package org.ilay;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

final class CollectionUtil {

    private CollectionUtil() {
    }

    static <T> Set<T> toNonEmptySet(T[] array) {
        Objects.requireNonNull(array);
        Check.arg(array.length != 0, "array must not be empty");
        return new HashSet<>(Arrays.asList(array));
    }
}
