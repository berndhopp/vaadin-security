package org.ilay;

import com.vaadin.util.CurrentInstance;

import org.ilay.api.Restrict;

import java.lang.reflect.Field;

class TestUtil {
    static void beforeTest() throws NoSuchFieldException, IllegalAccessException {
        Authorization.navigatorSupplier = new TestNavigatorSupplier();
        Authorization.sessionInitNotifierSupplier = new TestSessionInitNotifierSupplier();
        CurrentInstance.set(Restrict.class, null);
        final Field initialized = Authorization.class.getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.set(null, false);
    }
}
