package org.ilay.api;

/**
 * a Reverter removes bindings between permissions and {@link com.vaadin.navigator.View}s or
 * {@link com.vaadin.ui.Component}s that had been set up by a {@link Restrict} before.
 *
 * @author Bernd Hopp bernd@vaadin.com
 */
public interface Reverter {
    /**
     * reverts the {@link Restrict#to(Object)}- or {@link Restrict#to(Object...)}-operation
     * that returned the Reverter
     */
    void revert();
}
