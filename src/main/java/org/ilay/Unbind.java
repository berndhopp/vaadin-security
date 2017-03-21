package org.ilay;

public interface Unbind {
    void from(Object... permissions);

    void fromAll();
}
