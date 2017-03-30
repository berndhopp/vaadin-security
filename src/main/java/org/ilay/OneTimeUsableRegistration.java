package org.ilay;

abstract class OneTimeUsableRegistration implements Registration {

    private boolean used = false;

    @Override
    public void revert() {
        Check.state(!used, "revert() can only be called once");

        revertInternal();

        used = true;
    }

    abstract void revertInternal();
}
