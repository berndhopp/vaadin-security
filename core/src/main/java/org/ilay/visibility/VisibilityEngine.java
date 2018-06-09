package org.ilay.visibility;

import com.google.common.base.Strings;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.ListenerPriority;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toSet;

@ListenerPriority(Integer.MAX_VALUE - 2)
public class VisibilityEngine implements VaadinServiceInitListener, UIInitListener {

    private static final long serialVersionUID = 7808168756398878583L;
    private static final String PACKAGES_TO_SCAN_KEY = "ilay.packages_to_scan_for_visibility_engine";
    private static final String UPDATE_MODE_KEY = "ilay.update_mode";
    private static Map<Class<? extends HasElement>, Annotation> componentsToAnnotationsCache = new HashMap<>();
    private static Map<Class<? extends HasElement>, Map<Field, Annotation>> fieldsToAnnotationCache = new HashMap<>();
    private static UpdateMode updateMode = UpdateMode.disabled;
    private final Logger logger = LoggerFactory.getLogger(VisibilityEngine.class);

    @SuppressWarnings("unused")
    public static void permissionsChanged() {
        checkState(updateMode.equals(UpdateMode.manual), "update-mode must be set to manual to call this method");

        deepScan(UI.getCurrent()).forEach(VisibilityEngine::checkVisibility);
    }

    @SuppressWarnings("unchecked")
    private static void checkVisibility(ComponentAnnotationTuple tuple) {

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

    private static Stream<ComponentAnnotationTuple> deepScan(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren())
                .map(VisibilityEngine::flatScan)
                .flatMap(s -> s)
                .distinct();
    }

    @SuppressWarnings("unchecked")
    private static Stream<ComponentAnnotationTuple> flatScan(Component component) {
        final Class<? extends Component> componentClass = component.getClass();

        final Annotation annotationOnComponent = componentsToAnnotationsCache.get(componentClass);
        final Map<Field, Annotation> fieldAnnotationMap = fieldsToAnnotationCache.get(componentClass);

        Stream<ComponentAnnotationTuple> stream;

        if (fieldAnnotationMap != null) {
            stream = fieldAnnotationMap
                    .entrySet()
                    .stream()
                    .map(entry -> new ComponentAnnotationTuple(getComponentField(entry.getKey(), component), entry.getValue()));
        } else {
            stream = Stream.empty();
        }

        if (annotationOnComponent != null) {
            stream = Stream.concat(stream, Stream.of(new ComponentAnnotationTuple(component, annotationOnComponent)));
        }

        return stream;
    }

    private static Component getComponentField(Field field, Component component) {
        try {
            return (Component) field.get(component);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uiInit(UIInitEvent event) {
        final UI ui = event.getUI();

        switch (updateMode) {
            case manual:
                ui.addAttachListener(e -> flatScan(e.getSource()).forEach(VisibilityEngine::checkVisibility));
                break;
            case on_navigation:
                UiEventsHandler uiEventsHandler = new UiEventsHandler();

                ui.addAfterNavigationListener(uiEventsHandler);
                ui.addAttachListener(e -> uiEventsHandler.clearCache());
                ui.addDetachListener(e -> uiEventsHandler.removeComponentFromCache(e.getSource()));
                break;
        }
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        final VaadinService vaadinService = event.getSource();

        final DeploymentConfiguration deploymentConfiguration = vaadinService.getDeploymentConfiguration();
        final Properties initParameters = deploymentConfiguration.getInitParameters();

        final String packagesToScan = initParameters.getProperty(PACKAGES_TO_SCAN_KEY);

        if (Strings.isNullOrEmpty(packagesToScan)) {
            return;
        }

        initCaches(packagesToScan);

        updateMode = UpdateMode.valueOf(initParameters.getProperty(UPDATE_MODE_KEY, UpdateMode.on_navigation.name()));

        if (UpdateMode.on_navigation.equals(updateMode)) {
            vaadinService.addUIInitListener(this);
        }
    }

    @SuppressWarnings("unchecked")
    private void initCaches(String packagesToScan) {

        List<Object> params = new ArrayList(Arrays.asList(packagesToScan.split(","), new FieldAnnotationsScanner(), new TypeAnnotationsScanner(), new SubTypesScanner()));

        Reflections reflections = new Reflections(params.toArray());

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
                final Class<? extends Component> fieldType = (Class<? extends Component>) field.getType();

                checkState(fieldsToAnnotationCache.get(fieldType) == null, field + ": visibility-annotations cannot be used on both class-level and field-level");

                final Annotation annotation = field.getAnnotation((Class<? extends Annotation>) annotationClass);

                checkState(annotation != null);

                Class<? extends Component> declaringClass = (Class<? extends Component>) field.getDeclaringClass();

                final Map<Field, Annotation> fieldAnnotationMap = fieldsToAnnotationCache.computeIfAbsent(declaringClass, dc -> new HashMap<>());

                checkState(!fieldAnnotationMap.containsKey(field), field + "multiple visibility-annotations per field not allowed");

                field.setAccessible(true);

                fieldAnnotationMap.put(field, annotation);
            }
        }
    }

    private enum UpdateMode {
        manual,
        on_navigation,
        disabled
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

    private class UiEventsHandler implements AfterNavigationListener {

        private final Map<Component, Set<ComponentAnnotationTuple>> cache = new WeakHashMap<>();

        @Override
        public void afterNavigation(AfterNavigationEvent event) {
            event
                    .getActiveChain()
                    .stream()
                    .map(hasElement -> (Component) hasElement)
                    .map(component -> cache.computeIfAbsent(component, c -> deepScan(c).collect(toSet())))
                    .flatMap(Set::stream)
                    .forEach(VisibilityEngine::checkVisibility);
        }

        void clearCache() {
            cache.clear();
        }

        void removeComponentFromCache(Component component) {
            if (cache.remove(component) == null) {
                cache.values().forEach(tupleSet -> tupleSet.removeIf(tuple -> tuple.getComponent().equals(component)));
            }
        }
    }
}
