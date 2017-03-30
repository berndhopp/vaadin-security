package org.ilay;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

abstract class RestrictImpl<T> implements Restrict {

    final Map<T, Set<Object>> restrictionMap;

    RestrictImpl(T[] tArray) {
        Check.arraySanity(tArray);
        this.restrictionMap = new WeakHashMap<>(tArray.length);
        OpenBind.setCurrent(this);
    }

    RestrictImpl(T view) {
        requireNonNull(view);
        this.restrictionMap = new WeakHashMap<>(1);
        OpenBind.setCurrent(this);
    }

    @Override
    public Registration to(Object permission) {
        requireNonNull(permission);
        Check.openBindIs(this);
        final Set<Object> permissionSet = new CopyOnWriteArraySet<>();
        permissionSet.add(permission);

        for (Map.Entry<T, Set<Object>> tSetEntry : restrictionMap.entrySet()) {
            Set<Object> permissionForEntry = new CopyOnWriteArraySet<>(permissionSet);
            tSetEntry.setValue(permissionForEntry);
        }

        bindInternal();
        OpenBind.unsetCurrent();
        return asRegistration();
    }

    @Override
    public Registration to(Object... permissions) {
        Check.arraySanity(permissions);
        Check.openBindIs(this);

        Set<Object> permissionSet = new CopyOnWriteArraySet<>(asList(permissions));

        for (Map.Entry<T, Set<Object>> tSetEntry : restrictionMap.entrySet()) {
            Set<Object> permissionForEntry = new CopyOnWriteArraySet<>(permissionSet);
            tSetEntry.setValue(permissionForEntry);
        }

        bindInternal();
        OpenBind.unsetCurrent();
        return asRegistration();
    }

    protected abstract ObjectsRegistration<T> asRegistration();

    protected abstract void bindInternal();
}
