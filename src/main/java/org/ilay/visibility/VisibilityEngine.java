package org.ilay.visibility;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.ListenerPriority;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

import org.ilay.PermissionsChangedEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vaadin.flow.component.ComponentUtil.addListener;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;

@ListenerPriority(Integer.MAX_VALUE - 2)
public final class VisibilityEngine implements VaadinServiceInitListener, UIInitListener {

    private static final long serialVersionUID = 7808168756398878583L;
    private final Map<Class<? extends HasElement>, Optional<Annotation>> componentsToAnnotationsCache = new ConcurrentHashMap<>();
    private final Map<Class<? extends HasElement>, Map<Field, Annotation>> fieldsToAnnotationCache = new ConcurrentHashMap<>();

    @Override
    public void serviceInit(ServiceInitEvent e) {
        e.getSource().addUIInitListener(this);
    }

    @Override
    public void uiInit(UIInitEvent event) {
        final UI ui = event.getUI();

        deepScan(ui).forEach(this::checkVisibility);

        addListener(ui, PermissionsChangedEvent.class, e -> deepScan(ui).forEach(this::checkVisibility));
    }

    @SuppressWarnings("unchecked")
    private void checkVisibility(ComponentAnnotationTuple tuple) {

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
    }

    private Stream<ComponentAnnotationTuple> deepScan(Component component) {
        return concat(Stream.of(component), component.getChildren())
                .map(this::flatScan)
                .flatMap(s -> s);
    }

    @SuppressWarnings("unchecked")
    private Stream<ComponentAnnotationTuple> flatScan(Component component) {
        final Class<? extends Component> componentClass = component.getClass();

        final Optional<Annotation> annotationOnComponent = componentsToAnnotationsCache.computeIfAbsent(componentClass, this::getOptionalVisibilityAnnotation);

        final Map<Field, Annotation> fieldAnnotationMap = fieldsToAnnotationCache.computeIfAbsent(componentClass, this::getFieldsWithVisibilityAnnotationMap);

        Stream<ComponentAnnotationTuple> stream = fieldAnnotationMap
                .entrySet()
                .stream()
                .map(entry -> new ComponentAnnotationTuple(getComponentField(entry.getKey(), component), entry.getValue()));

        if (annotationOnComponent.isPresent()) {
            stream = concat(stream, Stream.of(new ComponentAnnotationTuple(component, annotationOnComponent.get())));
        }

        return stream;
    }

    private Map<Field, Annotation> getFieldsWithVisibilityAnnotationMap(Class<? extends HasElement> c) {
        return stream(c.getDeclaredFields())
                .filter(f -> {
                    int visibilityAnnotationsCount = (int) stream(f.getAnnotations())
                            .filter(a -> a.annotationType().isAnnotationPresent(VisibilityAnnotation.class))
                            .count();

                    switch (visibilityAnnotationsCount) {
                        case 0:
                            return false;
                        case 1:
                            return true;
                        default:
                            throw new IllegalStateException("more than one VisibilityAnnotation not allowed at " + f);
                    }
                })
                .collect(toMap(
                        f -> f,
                        f -> stream(f.getAnnotations())
                                .filter(a -> a.annotationType().isAnnotationPresent(VisibilityAnnotation.class))
                                .findFirst()
                                .get()
                        )
                );
    }

    private Optional<Annotation> getOptionalVisibilityAnnotation(Class<? extends HasElement> c) {
        final List<Annotation> visibilityAnnotations = stream(c.getAnnotations())
                .filter(a -> a.annotationType().isAnnotationPresent(VisibilityAnnotation.class))
                .collect(Collectors.toList());

        switch (visibilityAnnotations.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(visibilityAnnotations.get(0));
            default:
                throw new IllegalStateException("more than one VisibilityAnnotation not allowed at " + c);
        }
    }

    private Component getComponentField(Field field, Component component) {
        try {
            return (Component) field.get(component);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ComponentAnnotationTuple {
        private final Component component;
        private final Annotation annotation;

        private ComponentAnnotationTuple(Component component, Annotation annotation) {
            this.component = component;
            this.annotation = annotation;
        }

        Component getComponent() {
            return component;
        }

        Annotation getAnnotation() {
            return annotation;
        }
    }
}
