package org.ilay;

import java.util.function.Supplier;

/**
 * a supplier with an additional set method
 */
interface Vessel<T> extends Supplier<T> {
    void set(T t);
}
