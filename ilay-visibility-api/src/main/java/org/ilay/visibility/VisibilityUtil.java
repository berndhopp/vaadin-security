package org.ilay.visibility;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.VaadinService;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.stream;

class VisibilityUtil {

    private static final Map<Class<? extends Component>, AnnotationVisibilityEvaluatorTuple<?>> cache = new ConcurrentHashMap<>();

    static boolean hasVisibilityAnnotation(Class<?> clazz) {
        Objects.requireNonNull(clazz);

        final int visibilityAnnotationsCount = (int) stream(clazz.getAnnotations())
                .map(Annotation::annotationType)
                .filter(at -> at.isAnnotationPresent(VisibilityAnnotation.class))
                .count();

        switch (visibilityAnnotationsCount) {
            case 0:
                return false;
            case 1: {
                if (!Component.class.isAssignableFrom(clazz)) {
                    throw new IllegalStateException(clazz + " has visibilityAnnotation but is not a component");
                }
                return true;
            }
            default:
                throw new IllegalStateException("more than one visibilityAnnotation found at " + clazz);
        }
    }

    @SuppressWarnings("unchecked")
    static <ANNOTATION extends Annotation> void evaluateVisibility(Component component) {
        Objects.requireNonNull(component);

        AnnotationVisibilityEvaluatorTuple<ANNOTATION> tuple = (AnnotationVisibilityEvaluatorTuple<ANNOTATION>) cache.computeIfAbsent(
                component.getClass(),
                c -> stream(c.getAnnotations())
                        .filter(a -> a.annotationType().isAnnotationPresent(VisibilityAnnotation.class))
                        .findFirst()
                        .map(a -> new AnnotationVisibilityEvaluatorTuple(a, a.annotationType().getAnnotation(VisibilityAnnotation.class).value()))
                        .orElseThrow(IllegalStateException::new)
        );

        component.setVisible(
                VaadinService
                        .getCurrent()
                        .getInstantiator()
                        .getOrCreate(tuple.getVisibilityEvaluatorClass())
                        .evaluateVisibility(tuple.getAnnotation())
        );
    }

    private static class AnnotationVisibilityEvaluatorTuple<ANNOTATION extends Annotation> {
        private final ANNOTATION annotation;
        private final Class<? extends VisibilityEvaluator<ANNOTATION>> visibilityEvaluatorClass;

        AnnotationVisibilityEvaluatorTuple(ANNOTATION annotation, Class<? extends VisibilityEvaluator<ANNOTATION>> visibilityEvaluatorClass) {
            this.annotation = annotation;
            this.visibilityEvaluatorClass = visibilityEvaluatorClass;
        }

        ANNOTATION getAnnotation() {
            return annotation;
        }

        Class<? extends VisibilityEvaluator<ANNOTATION>> getVisibilityEvaluatorClass() {
            return visibilityEvaluatorClass;
        }
    }
}
