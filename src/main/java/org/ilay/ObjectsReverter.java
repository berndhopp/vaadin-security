package org.ilay;

import java.util.Map;
import java.util.Set;

abstract class ObjectsReverter<T> extends OneTimeUsableReverter {

    final Map<T, Set<Object>> restrictionsMap;

    ObjectsReverter(Map<T, Set<Object>> restrictionsMap) {
        this.restrictionsMap = Check.notNullOrEmpty(restrictionsMap);
    }

    abstract void revertInternal();
}
