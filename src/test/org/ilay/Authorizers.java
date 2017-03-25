package org.ilay;

import org.ilay.api.InMemoryAuthorizer;

public class Authorizers {
    static final InMemoryAuthorizer FOO_AUTHORIZER = new InMemoryAuthorizer<Foo>() {
        @Override
        public boolean isGranted(Foo foo) {
            return false;
        }

        @Override
        public Class<Foo> getPermissionClass() {
            return Foo.class;
        }
    };

    static final InMemoryAuthorizer BAR_AUTHORIZER = new InMemoryAuthorizer<Bar>() {
        @Override
        public boolean isGranted(Bar foo) {
            return false;
        }

        @Override
        public Class<Bar> getPermissionClass() {
            return Bar.class;
        }
    };

    static final InMemoryAuthorizer STRING_AUTHORIZER = new InMemoryAuthorizer<String>() {
        @Override
        public boolean isGranted(String s) {
            return false;
        }

        @Override
        public Class<String> getPermissionClass() {
            return String.class;
        }
    };
    static final InMemoryAuthorizer INTEGER_AUTHORIZER = new InMemoryAuthorizer<Integer>() {
        @Override
        public boolean isGranted(Integer integer) {
            return false;
        }

        @Override
        public Class<Integer> getPermissionClass() {
            return Integer.class;
        }
    };
    static final InMemoryAuthorizer OBJECT_AUTHORIZER = new InMemoryAuthorizer<Object>() {

        @Override
        public boolean isGranted(Object o) {
            return false;
        }

        @Override
        public Class<Object> getPermissionClass() {
            return Object.class;
        }
    };

}
