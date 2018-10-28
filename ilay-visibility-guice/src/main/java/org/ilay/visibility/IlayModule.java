package org.ilay.visibility;

class IlayModule extends com.google.inject.AbstractModule {
    public void configure() {
        bindListener(new VisibilityAnnotationMatcher(), new VisibilityProvisionListener());
    }
}
