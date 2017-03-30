package org.ilay;

import org.ilay.api.Reverter;

abstract class OneTimeUsableReverter implements Reverter {

    private boolean used = false;

    @Override
    public void revert() {
        Check.state(!used, "revert() can only be called once");

        revertInternal();

        used = true;
    }

    abstract void revertInternal();
}
