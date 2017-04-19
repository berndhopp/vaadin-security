package org.ilay;

class FooTypedAuthorizationView extends TypedAuthorizationView<Foo> {

    private final boolean working;

    FooTypedAuthorizationView(boolean working) {
        this.working = working;
    }

    @Override
    protected Foo parse(String parameters) throws ParseException {
        if (working) {
            return new Foo();
        } else {
            throw new ParseException();
        }
    }

    @Override
    public void enter(Foo foo) {
    }
}
