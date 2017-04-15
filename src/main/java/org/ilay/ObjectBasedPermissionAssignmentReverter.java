package org.ilay;

import java.util.Map;
import java.util.Set;

/**
 * reverts all operations on 'objects' ( instead of 'data' ), like
 * Authorization.restrictComponent(), Authorization.restrictView()
 * @author Bernd Hopp bernd@vaadin.com
 * */
abstract class ObjectBasedPermissionAssignmentReverter<T> extends OneTimeUsableReverter {

    private final Map<T, Set<Object>> restrictionsMap;

    ObjectBasedPermissionAssignmentReverter(Map<T, Set<Object>> restrictionsMap) {
        this.restrictionsMap = Check.notNullOrEmpty(restrictionsMap);
    }

    Map<T, Set<Object>> getRestrictionsMap() {
        return restrictionsMap;
    }
}
