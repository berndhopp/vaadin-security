package org.ilay;

import com.vaadin.flow.router.BeforeEnterEvent;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public abstract class Access implements Serializable {

    private static final long serialVersionUID = -5142617945164430893L;

    private Access() {
    }

    public static Access restricted(Class<? extends Exception> errorTarget, String errorMessage) {
        Objects.requireNonNull(errorTarget, "errorTarget must not be null");

        return new AccessRestrictedToErrorClass(errorTarget, errorMessage);
    }

    public static Access granted() {
        return new AccessGranted();
    }

    public static Access restricted(Class<? extends Exception> errorTarget) {
        Objects.requireNonNull(errorTarget, "errorTarget must not be null");

        return new AccessRestrictedToErrorClass(errorTarget);
    }

    public static Access restricted(String rerouteTarget) {
        Objects.requireNonNull(rerouteTarget, "rerouteTarget must not be null");

        return new AccessRestricted(rerouteTarget);
    }

    public static Access restricted(String rerouteTarget, Object... parameters) {
        Objects.requireNonNull(rerouteTarget, "rerouteTarget must not be null");

        return new AccessRestricted(rerouteTarget, parameters);
    }

    abstract void exec(BeforeEnterEvent enterEvent);

    static class AccessGranted extends Access {
        private static final long serialVersionUID = -3208933626653467204L;

        @Override
        void exec(BeforeEnterEvent enterEvent) {
        }
    }

    static class AccessRestricted extends Access {

        private static final long serialVersionUID = -8979204975219042421L;
        private final String rerouteTarget;
        private final Object[] parameters;

        private AccessRestricted(String rerouteTarget) {
            this.rerouteTarget = rerouteTarget;
            parameters = null;
        }

        private AccessRestricted(String rerouteTarget, Object[] parameters) {
            this.rerouteTarget = rerouteTarget;
            this.parameters = parameters;
        }

        @Override
        void exec(BeforeEnterEvent enterEvent) {
            if (parameters != null) {
                if (parameters.length == 1) {
                    enterEvent.rerouteTo(rerouteTarget, parameters[0]);
                } else {
                    enterEvent.rerouteTo(rerouteTarget, Arrays.asList(parameters));
                }
            } else {
                enterEvent.rerouteTo(rerouteTarget);
            }

        }
    }

    static class AccessRestrictedToErrorClass extends Access {

        private static final long serialVersionUID = -8979204975219042421L;
        private final Class<? extends Exception> exceptionClass;
        private final String errorMessage;

        private AccessRestrictedToErrorClass(Class<? extends Exception> exceptionClass) {
            this.exceptionClass = exceptionClass;
            this.errorMessage = null;
        }

        private AccessRestrictedToErrorClass(Class<? extends Exception> exceptionClass, String errorMessage) {
            this.exceptionClass = exceptionClass;
            this.errorMessage = errorMessage;
        }

        @Override
        void exec(BeforeEnterEvent enterEvent) {
            if (errorMessage == null) {
                enterEvent.rerouteToError(exceptionClass);
            } else {
                enterEvent.rerouteToError(exceptionClass, errorMessage);
            }
        }
    }
}

