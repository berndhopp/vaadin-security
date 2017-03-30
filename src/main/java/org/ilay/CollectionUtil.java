package org.ilay;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class CollectionUtil {

    private CollectionUtil() {
    }

    static <T> Set<Reference<T>> toNonEmptyReferenceSet(T t) {
        Reference<T> reference = new WeakReference<>(t);
        return Collections.singleton(reference);
    }

    static <T> Set<Reference<T>> toNonEmptyReferenceSet(T[] array) {
        Set<Reference<T>> set = new HashSet<>(array.length);

        for (T t : array) {
            set.add(new WeakReference<>(t));
        }

        return set;
    }

    static <T> Set<T> toNonEmptyCOWSet(T[] array) {
        List<T> tList = Arrays.asList(array);
        return new CopyOnWriteArraySet<>(tList);
    }

    static <T> Set<T> toNonEmptySet(T[] array) {
        Set<T> set = new HashSet<>(array.length);
        Collections.addAll(set, array);
        return set;
    }
}
