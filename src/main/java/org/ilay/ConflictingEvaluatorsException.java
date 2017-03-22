package org.ilay;

import static java.lang.String.format;

public class ConflictingEvaluatorsException extends RuntimeException {

    ConflictingEvaluatorsException(Authorizer authorizer1, Authorizer authorizer2, Class permissionClass) {
        super(
                format(
                        "conflicting navigators: %s and %s are both assignable to %s",
                        authorizer1,
                        authorizer2,
                        permissionClass)
        );
    }
}
