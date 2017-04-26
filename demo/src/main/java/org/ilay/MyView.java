package org.ilay;

public class MyView extends org.ilay.TypedAuthorizationView<MyView.Foo> {

    @Override
    protected Foo parse(String parameters) throws ParseException {
        return null;
    }

    @Override
    protected void enter(Foo foo) {
        //set components according to the parsed foo
    }

    public static class Foo {

    }
}
