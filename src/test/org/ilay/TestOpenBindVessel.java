package org.ilay;

class TestOpenBindVessel implements VaadinAbstraction.Vessel<OpenBind> {

    private OpenBind openBind;

    @Override
    public void set(OpenBind openBind) {
        this.openBind = openBind;
    }

    @Override
    public OpenBind get() {
        return openBind;
    }
}
