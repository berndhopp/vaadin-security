package org.ilay;

import static java.lang.String.format;

public class ConflictingEvaluatorsException extends RuntimeException {

    ConflictingEvaluatorsException(Evaluator evaluator1, Evaluator evaluator2, Class permissionClass) {
        super(
                format(
                        "conflicting navigators: %s and %s are both assignable to %s",
                        evaluator1,
                        evaluator2,
                        permissionClass)
        );
    }
}
