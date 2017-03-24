package org.ilay;

import java.lang.reflect.Field;

class TestUtil {
    static void beforeTest() throws NoSuchFieldException, IllegalAccessException {
        AuthorizationContext.currentInstanceVessel = new TestAuthorizationContextVessel();
        Authorization.navigatorSupplier = new TestNavigatorSupplier();
        Authorization.sessionInitNotifierSupplier = new TestSessionInitNotifierSupplier();
        final Field initialized = Authorization.class.getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.set(null, false);
    }
}
