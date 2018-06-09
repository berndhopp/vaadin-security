package org.ilay.actions;

import com.vaadin.flow.server.VaadinService;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

class GuardedActionInterceptor implements MethodInterceptor {

    private static final Map<Method, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation methodInvocation) {

        CacheEntry cacheEntry = cache.computeIfAbsent(methodInvocation.getMethod(), method -> {

            if (!Void.TYPE.equals(method.getReturnType())) {
                throw new IllegalStateException();
            }

            final List<Annotation> annotations = stream(method.getAnnotations())
                    .filter(a -> a.annotationType().isAnnotationPresent(GuardedActionAnnotation.class))
                    .collect(toList());

            if (annotations.size() != 1) {
                throw new IllegalStateException();
            }

            final Annotation annotation = annotations.get(0);

            final Class<? extends ActionGuard> actionGuardClass = annotation
                    .annotationType()
                    .getAnnotation(GuardedActionAnnotation.class)
                    .value();

            return new CacheEntry(actionGuardClass, annotation);
        });

        VaadinService
                .getCurrent()
                .getInstantiator()
                .getOrCreate(cacheEntry.getEvaluator())
                .ifAllowed(cacheEntry.getAnnotation(), () -> {
                            try {
                                methodInvocation.proceed();
                            } catch (Throwable throwable) {
                                throw new RuntimeException(throwable);
                            }
                        }
                );

        return null;
    }

    static class CacheEntry {
        private final Class<? extends ActionGuard> evaluator;
        private final Annotation annotation;

        CacheEntry(Class<? extends ActionGuard> evaluator, Annotation annotation) {
            this.evaluator = evaluator;
            this.annotation = annotation;
        }

        Annotation getAnnotation() {
            return annotation;
        }

        Class<? extends ActionGuard> getEvaluator() {
            return evaluator;
        }
    }
}
