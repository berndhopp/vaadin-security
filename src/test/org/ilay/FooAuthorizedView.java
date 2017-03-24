package org.ilay;

public class FooAuthorizedView extends AuthorizedView<Foo> {

    final boolean working;

    public FooAuthorizedView(boolean working) {
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
    public void onSuccessfulAuthorization(Foo foo) {
    }

    @Override
    public void onParseException(ParseException parseException) {
        super.onParseException(parseException);
    }

    @Override
    public void onFailedAuthorization(Foo foo) {
        super.onFailedAuthorization(foo);
    }
}
