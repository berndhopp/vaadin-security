package org.ilay;

public class Evaluators {
    static final Evaluator fooEvaluator = new Evaluator<Foo>() {
        @Override
        public boolean evaluate(Foo foo) {
            return false;
        }

        @Override
        public Class<Foo> getPermissionClass() {
            return Foo.class;
        }
    };
    static final Evaluator stringEvaluator = new Evaluator<String>() {
        @Override
        public boolean evaluate(String s) {
            return false;
        }

        @Override
        public Class<String> getPermissionClass() {
            return String.class;
        }
    };
    static final Evaluator integerEvaluator = new Evaluator<Integer>() {
        @Override
        public boolean evaluate(Integer integer) {
            return false;
        }

        @Override
        public Class<Integer> getPermissionClass() {
            return Integer.class;
        }
    };
    static final Evaluator objectEvaluator = new Evaluator<Object>() {

        @Override
        public boolean evaluate(Object o) {
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
