package org.ilay;

public class FooTypedAuthorizationView extends TypedAuthorizationView<Foo> {

    final boolean working;

    public FooTypedAuthorizationView(boolean working) {
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
