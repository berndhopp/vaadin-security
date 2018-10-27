package org.ilay.guice;

import com.google.inject.spi.ProvisionListener;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;

import org.ilay.PermissionsChangedEvent;
import org.ilay.visibility.VisibilityAnnotation;
import org.ilay.visibility.VisibilityEvaluator;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.vaadin.flow.component.ComponentUtil.addListener;
import static java.util.Arrays.stream;

class VisibilityProvisionListener implements ProvisionListener {

    private final Map<Class<? extends Component>, AnnotationVisibilityEvaluatorTuple> annotationMap = new ConcurrentHashMap<>();

    private static class AnnotationVisibilityEvaluatorTuple{
        private final Annotation annotation;
        private final Class<? extends VisibilityEvaluator<?, ?>> visibilityEvaluatorClass;

        private AnnotationVisibilityEvaluatorTuple(Annotation annotation, Class<? extends VisibilityEvaluator<?, ?>> visibilityEvaluatorClass) {
            this.annotation = annotation;
            this.visibilityEvaluatorClass = visibilityEvaluatorClass;
        }

        Annotation getAnnotation() {
            return annotation;
        }

        Class<? extends VisibilityEvaluator<?, ?>> getVisibilityEvaluatorClass() {
            return visibilityEvaluatorClass;
        }
    }

    public <T> void onProvision(ProvisionInvocation<T> provision) {
        final Component component = (Component)provision.provision();

        final AnnotationVisibilityEvaluatorTuple tuple = annotationMap.computeIfAbsent(component.getClass(), c ->
            stream(c.getAnnotations())
                .filter(a -> a.annotationType().isAnnotationPresent(VisibilityAnnotation.class))
                .findFirst()
                .map(a -> new AnnotationVisibilityEvaluatorTuple(a, a.annotationType().getAnnotation(VisibilityAnnotation.class).value()))
                .orElseThrow(IllegalStateException::new)
        );

        addListener(UI.getCurrent(), PermissionsChangedEvent.class, e -> setVisibility(component, tuple));
        setVisibility(component, tuple);
    }

    private void setVisibility(Component component, AnnotationVisibilityEvaluatorTuple tuple) {
        VisibilityEvaluator visibilityEvaluator = VaadinService
                .getCurrent()
                .getInstantiator()
                .getOrCreate(tuple.getVisibilityEvaluatorClass());

        boolean visible = visibilityEvaluator.evaluateVisibility(component, tuple.getAnnotation());

        component.setVisible(visible);
    }
}
