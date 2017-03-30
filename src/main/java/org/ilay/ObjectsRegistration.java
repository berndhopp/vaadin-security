package org.ilay;

import java.util.Map;
import java.util.Set;

abstract class ObjectsRegistration<T> extends OneTimeUsableRegistration {

    final Map<T, Set<Object>> restrictionsMap;

    ObjectsRegistration(Map<T, Set<Object>> restrictionsMap) {
        this.restrictionsMap = Check.notNullOrEmpty(restrictionsMap);
    }

    abstract void revertInternal();
}
