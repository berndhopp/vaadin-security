package org.ilay;

public class Evaluators {
    static final Authorizer FOO_AUTHORIZER = new Authorizer<Foo>() {
        @Override
        public boolean isGranted(Foo foo) {
            return false;
        }

        @Override
        public Class<Foo> getPermissionClass() {
            return Foo.class;
        }
    };
    static final Authorizer STRING_AUTHORIZER = new Authorizer<String>() {
        @Override
        public boolean isGranted(String s) {
            return false;
        }

        @Override
        public Class<String> getPermissionClass() {
            return String.class;
        }
    };
    static final Authorizer INTEGER_AUTHORIZER = new Authorizer<Integer>() {
        @Override
        public boolean isGranted(Integer integer) {
            return false;
        }

        @Override
        public Class<Integer> getPermissionClass() {
            return Integer.class;
        }
    };
    static final Authorizer OBJECT_AUTHORIZER = new Authorizer<Object>() {

        @Override
        public boolean isGranted(Object o) {
            return false;
        }

        @Override
        public Class<Object> getPermissionClass() {
            return Object.class;
        }
    };

    static class Foo {
    }

    static class Bar extends Foo {
    }
}
