package org.ilay;

import com.vaadin.shared.Registration;

/**
 * a Registration that will throw an exception if revert() is attempted to be called more than once
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
abstract class RegistrationBase implements Registration {

    private static final long serialVersionUID = 2661609489660482094L;
    private boolean used = false;

    public void remove() {
        Check.state(!used, "revert() can only be called once");

        revertInternal();

        used = true;
    }

    abstract void revertInternal();
}
