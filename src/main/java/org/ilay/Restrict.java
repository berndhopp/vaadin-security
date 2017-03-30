package org.ilay;

public interface Restrict {
    Registration to(Object permission);

    Registration to(Object... permissions);
}
