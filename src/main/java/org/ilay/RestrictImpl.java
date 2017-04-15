package org.ilay;

import org.ilay.api.Restrict;
import org.ilay.api.Reverter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * @author Bernd Hopp bernd@vaadin.com
 */
abstract class RestrictImpl<T> implements Restrict {

    final Map<T, Set<Object>> restrictionMap;

    RestrictImpl(T[] tArray) {
        Check.arraySanity(tArray);
        Check.noUnclosedRestrict();

        this.restrictionMap = new WeakHashMap<>(tArray.length);

        for (T t : tArray) {
            restrictionMap.put(t, new CopyOnWriteArraySet<>());
        }

        Check.setCurrentRestrict(this);
    }

    RestrictImpl(T t) {
        requireNonNull(t);
        Check.noUnclosedRestrict();

        this.restrictionMap = new WeakHashMap<>(1);

        restrictionMap.put(t, new CopyOnWriteArraySet<>());

        Check.setCurrentRestrict(this);
    }

    @Override
    public Reverter to(Object permission) {
        requireNonNull(permission);
        Check.currentRestrictIs(this);

        for (Map.Entry<T, Set<Object>> tSetEntry : restrictionMap.entrySet()) {
            Set<Object> permissionForEntry = tSetEntry.getValue();
            permissionForEntry.add(permission);
        }

        bindInternal();

        Check.setCurrentRestrict(null);

        return createReverter();
    }

    @Override
    public Reverter to(Object... permissions) {
        Check.arraySanity(permissions);
        Check.currentRestrictIs(this);

        List<Object> permissionList = asList(permissions);

        for (Set<Object> permissionsEntry : restrictionMap.values()) {
            permissionsEntry.addAll(permissionList);
        }

        bindInternal();
        Check.setCurrentRestrict(null);
        return createReverter();
    }

    protected abstract Reverter createReverter();

    protected abstract void bindInternal();
}
