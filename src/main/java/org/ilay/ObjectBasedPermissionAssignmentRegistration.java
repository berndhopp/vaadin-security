package org.ilay;

import java.util.Map;
import java.util.Set;

/**
 * reverts all operations on 'objects' ( instead of 'data' ), like Authorization.restrictComponent(),
 * Authorization.restrictView()
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
abstract class ObjectBasedPermissionAssignmentRegistration<T> extends RegistrationBase {

    private static final long serialVersionUID = -4711028984633062646L;
    private final Map<T, Set<Object>> restrictionsMap;

    ObjectBasedPermissionAssignmentRegistration(Map<T, Set<Object>> restrictionsMap) {
        Check.notNullOrEmpty(restrictionsMap);
        this.restrictionsMap = restrictionsMap;
    }

    Map<T, Set<Object>> getRestrictionsMap() {
        return restrictionsMap;
    }
}
