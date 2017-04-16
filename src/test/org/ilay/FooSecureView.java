package org.ilay;

public class FooSecureView extends SecureView<Foo> {

    final boolean working;

    public FooSecureView(boolean working) {
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
