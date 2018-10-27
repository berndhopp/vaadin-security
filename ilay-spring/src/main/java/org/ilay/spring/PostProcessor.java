package org.ilay.spring;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;

import org.ilay.PermissionsChangedEvent;
import org.ilay.visibility.VisibilityAnnotation;
import org.ilay.visibility.VisibilityEvaluator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.stream;

@org.springframework.stereotype.Component
class PostProcessor implements BeanPostProcessor {

    private final Map<Class<? extends Component>, Optional<AnnotationVisibilityEvaluatorTuple>> annotationMap = new ConcurrentHashMap<>();

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if (!(bean instanceof Component)) {
            return bean;
        }

        Component component = (Component) bean;

        annotationMap.computeIfAbsent(component.getClass(), c ->
                stream(c.getAnnotations())
                        .filter(a -> a.annotationType().isAnnotationPresent(VisibilityAnnotation.class))
                        .findFirst()
                        .map(a -> new AnnotationVisibilityEvaluatorTuple(a, a.annotationType().getAnnotation(VisibilityAnnotation.class).value()))
        ).ifPresent(tuple -> {
            ComponentUtil.addListener(UI.getCurrent(), PermissionsChangedEvent.class, e -> setVisibility(component, tuple));
            setVisibility(component, tuple);
        });

        return component;
    }

    private void setVisibility(Component component, AnnotationVisibilityEvaluatorTuple tuple) {
        VisibilityEvaluator visibilityEvaluator = VaadinService
                .getCurrent()
                .getInstantiator()
                .getOrCreate(tuple.getVisibilityEvaluatorClass());

        boolean visible = visibilityEvaluator.evaluateVisibility(component, tuple.getAnnotation());

        component.setVisible(visible);
    }

    private static class AnnotationVisibilityEvaluatorTuple {
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
}
