package org.ilay.api;

public interface Restrict {
    Reverter to(Object permission);

    Reverter to(Object... permissions);
}
