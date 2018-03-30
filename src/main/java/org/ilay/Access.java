package org.ilay;

import com.vaadin.flow.function.SerializableConsumer;

import java.io.Serializable;
import java.util.Objects;

public abstract class Access implements Serializable {

    private static final long serialVersionUID = -5142617945164430893L;

    private Access() {
    }

    public static Access granted() {
        return new AccessGranted();
    }

    public static Access restricted(String rerouteTarget) {
        Objects.requireNonNull(rerouteTarget, "rerouteTarget must not be null");
        if (rerouteTarget.isEmpty())
            throw new IllegalArgumentException("rerouteTarget must not be empty");

        return new AccessRestricted(rerouteTarget);
    }

    abstract void ifRestricted(SerializableConsumer<String> consumer);

    private static class AccessGranted extends Access {
        private static final long serialVersionUID = -3208933626653467204L;

        @Override
        public void ifRestricted(SerializableConsumer<String> consumer) {
        }
    }

    private static class AccessRestricted extends Access {

        private static final long serialVersionUID = -8979204975219042421L;
        private final String rerouteTarget;

        private AccessRestricted(String rerouteTarget) {
            this.rerouteTarget = rerouteTarget;
        }

        @Override
        public void ifRestricted(SerializableConsumer<String> consumer) {
            consumer.accept(rerouteTarget);
        }
    }
}

