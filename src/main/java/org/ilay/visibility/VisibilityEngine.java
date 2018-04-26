package org.ilay.visibility;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.ListenerPriority;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

@ListenerPriority(Integer.MAX_VALUE - 2)
public class VisibilityEngine implements VaadinServiceInitListener, UIInitListener, BeforeEnterListener {

    private static final long serialVersionUID = 7808168756398878583L;
    private final Map<Class<? extends Component>, Annotation> componentsToAnnotationsCache = new HashMap<>();
    private final Map<Class<? extends Component>, Map<Field, Annotation>> fieldsToAnnotationCache = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void serviceInit(ServiceInitEvent event) {

        Reflections reflections = new Reflections();

        final Set<Class<?>> visibilityAnnotations = reflections.getTypesAnnotatedWith(VisibilityAnnotation.class);

        for (Class<?> annotationClass : visibilityAnnotations) {

            checkState(annotationClass.isAnnotation(), "VisibilityAnnotation must only be attached to Annotations");

            final Set<Class<?>> componentsWithVisibilityAnnotation = reflections.getTypesAnnotatedWith((Class<? extends Annotation>) annotationClass);

            for (Class<?> componentClass : componentsWithVisibilityAnnotation) {
                checkState(Component.class.isAssignableFrom(componentClass), "VisibilityAnnotations must only be attached to components or fields of components");

                final Annotation annotation = componentClass.getAnnotation((Class<? extends Annotation>) annotationClass);

                componentsToAnnotationsCache.put((Class<? extends Component>) componentClass, annotation);
            }
        }

        for (Class<?> annotationClass : visibilityAnnotations) {

            final Set<Field> fieldsWithVisibilityAnnotation = reflections.getFieldsAnnotatedWith((Class<? extends Annotation>) annotationClass);

            for (Field field : fieldsWithVisibilityAnnotation) {
                final Class<?> fieldType = field.getType();

                checkState(fieldsToAnnotationCache.get(fieldType) == null, field + ": visibility-annotations cannot be used on both class-level and field-level");

                final Annotation annotation = field.getAnnotation((Class<? extends Annotation>) annotationClass);

                checkState(annotation != null);

                Class<? extends Component> declaringClass = (Class<? extends Component>) field.getDeclaringClass();

                final Map<Field, Annotation> fieldAnnotationMap = fieldsToAnnotationCache.computeIfAbsent(declaringClass, dc -> new HashMap<>());

                checkState(!fieldAnnotationMap.containsKey(field), field + "multiple visibility-annotations per field not allowed");

                fieldAnnotationMap.put(field, annotation);
            }
        }

        event.getSource().addUIInitListener(this);
    }

    @Override
    public void uiInit(UIInitEvent event) {
        event.getUI().addBeforeEnterListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void beforeEnter(BeforeEnterEvent event) {
        event
                .getUI()
                .getChildren()
                .flatMap(this::getComponentAnnotationTuple)
                .forEach(tuple -> {
                    final Component component = tuple.getComponent();
                    final Annotation annotation = tuple.getAnnotation();

                    final VisibilityAnnotation visibilityAnnotation = annotation
                            .annotationType()
                            .getAnnotation(VisibilityAnnotation.class);

                    final VisibilityEvaluator visibilityEvaluator = VaadinService
                            .getCurrent()
                            .getInstantiator()
                            .getOrCreate(visibilityAnnotation.value());

                    final boolean visibility = visibilityEvaluator.evaluateVisibility(component, annotation);

                    component.setVisible(visibility);
                });
    }

    @SuppressWarnings("unchecked")
    private Stream<ComponentAnnotationTuple> getComponentAnnotationTuple(Component component) {
        final Class<? extends Component> componentClass = component.getClass();

        final Annotation annotationOnComponent = componentsToAnnotationsCache.get(componentClass);
        final Map<Field, Annotation> fieldAnnotationMap = fieldsToAnnotationCache.get(componentClass);

        final Stream<ComponentAnnotationTuple> streamOfParentComponent;

        if (annotationOnComponent != null) {
            streamOfParentComponent = Stream.of(new ComponentAnnotationTuple(component, annotationOnComponent));
        } else {
            streamOfParentComponent = Stream.empty();
        }

        final Stream<ComponentAnnotationTuple> streamOfComponentFields;

        if (fieldAnnotationMap != null) {
            streamOfComponentFields = fieldAnnotationMap
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        final Field field = entry.getKey();

                        Component fieldComponent;

                        try {
                            fieldComponent = (Component) field.get(component);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                        return new ComponentAnnotationTuple(fieldComponent, entry.getValue());
                    });
        } else {
            streamOfComponentFields = Stream.empty();
        }

        return Stream.concat(streamOfParentComponent, streamOfComponentFields);
    }

    private static final class ComponentAnnotationTuple {
        private final Component component;
        private final Annotation annotation;

        private ComponentAnnotationTuple(Component component, Annotation annotation) {
            this.component = component;
            this.annotation = annotation;
        }

        public Component getComponent() {
            return component;
        }

        public Annotation getAnnotation() {
            return annotation;
        }
    }
}
