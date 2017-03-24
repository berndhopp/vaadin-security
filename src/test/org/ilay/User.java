package org.ilay;

import java.util.HashSet;
import java.util.Set;

public class User {
    private final Set<String> roles = new HashSet<>();
    private Clearance clearance = Clearance.NON;

    Set<String> getRoles() {
        return roles;
    }

    Clearance getClearance() {
        return clearance;
    }

    void setClearance(Clearance clearance) {
        this.clearance = clearance;
    }
}
