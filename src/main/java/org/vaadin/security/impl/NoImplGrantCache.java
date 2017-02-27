package org.vaadin.security.impl;

import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NoImplGrantCache implements Object2BooleanMap<Object> {

    static final Object2BooleanMap<Object> INSTANCE = new NoImplGrantCache();

    @Override
    public ObjectSet<Map.Entry<Object, Boolean>> entrySet() {
        return null;
    }

    @Override
    public Boolean getOrDefault(Object key, Boolean defaultValue) {
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super Object, ? super Boolean> action) {

    }

    @Override
    public void replaceAll(BiFunction<? super Object, ? super Boolean, ? extends Boolean> function) {

    }

    @Override
    public Boolean putIfAbsent(Object key, Boolean value) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(Object key, Boolean oldValue, Boolean newValue) {
        return false;
    }

    @Override
    public Boolean replace(Object key, Boolean value) {
        return null;
    }

    @Override
    public Boolean computeIfAbsent(Object key, Function<? super Object, ? extends Boolean> mappingFunction) {
        return null;
    }

    @Override
    public Boolean computeIfPresent(Object key, BiFunction<? super Object, ? super Boolean, ? extends Boolean> remappingFunction) {
        return null;
    }

    @Override
    public Boolean compute(Object key, BiFunction<? super Object, ? super Boolean, ? extends Boolean> remappingFunction) {
        return null;
    }

    @Override
    public Boolean merge(Object key, Boolean value, BiFunction<? super Boolean, ? super Boolean, ? extends Boolean> remappingFunction) {
        return null;
    }

    @Override
    public ObjectSet<Entry<Object>> object2BooleanEntrySet() {
        return null;
    }

    @Override
    public ObjectSet<Object> keySet() {
        return null;
    }

    @Override
    public BooleanCollection values() {
        return null;
    }

    @Override
    public boolean containsValue(boolean value) {
        return false;
    }

    @Override
    public boolean put(Object key, boolean value) {
        return false;
    }

    @Override
    public boolean getBoolean(Object key) {
        return false;
    }

    @Override
    public boolean removeBoolean(Object key) {
        return false;
    }

    @Override
    public void defaultReturnValue(boolean rv) {

    }

    @Override
    public boolean defaultReturnValue() {
        return false;
    }

    @Override
    public Boolean put(Object key, Boolean value) {
        return null;
    }

    @Override
    public Boolean get(Object key) {
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Boolean remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<?, ? extends Boolean> m) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void clear() {

    }
}
