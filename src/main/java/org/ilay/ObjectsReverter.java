package org.ilay;

import java.util.Map;
import java.util.Set;

//reverts all operations on 'objects' ( instead of 'data' ), like
//Authorization.restrictComponent(), Authorization.restrictView()
abstract class ObjectsReverter<T> extends OneTimeUsableReverter {

    final Map<T, Set<Object>> restrictionsMap;

    ObjectsReverter(Map<T, Set<Object>> restrictionsMap) {
        this.restrictionsMap = Check.notNullOrEmpty(restrictionsMap);
    }
}
