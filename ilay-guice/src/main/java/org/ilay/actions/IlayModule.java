package org.ilay.actions;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static java.util.Arrays.stream;

class IlayModule extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(
                new AbstractMatcher<Class<?>>() {
                    @Override
                    public boolean matches(Class<?> aClass) {
                        return true;
                    }
                },
                new AbstractMatcher<Method>() {
                    @Override
                    public boolean matches(Method method) {
                        return stream(method.getAnnotations())
                                .map(Annotation::annotationType)
                                .anyMatch(at -> at.isAnnotationPresent(GuardedActionAnnotation.class));
                    }
                },
                new GuardedActionInterceptor()
        );
    }
}
