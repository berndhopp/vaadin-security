package org.ilay.visibility;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toMap;

@ListenerPriority(Integer.MAX_VALUE - 2)
public class VisibilityEngine implements VaadinServiceInitListener, UIInitListener {

    private static final long serialVersionUID = 7808168756398878583L;
    private static final String PACKAGES_TO_SCAN_KEY = "ilay.packages_to_scan_for_visibility_engine";
    private static Map<Class<? extends HasElement>, Annotation> componentsToAnnotationsCache = new HashMap<>();
    private static Map<Class<? extends HasElement>, Map<Field, Annotation>> fieldsToAnnotationCache = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(VisibilityEngine.class);

    @Override
    @SuppressWarnings("unchecked")
    public void serviceInit(ServiceInitEvent event) {
        final VaadinService vaadinService = event.getSource();

        initCaches(vaadinService);

        vaadinService.addUIInitListener(this);
    }

    @SuppressWarnings("unchecked")
    private void initCaches(VaadinService vaadinService) {
        final DeploymentConfiguration deploymentConfiguration = vaadinService.getDeploymentConfiguration();
        final Properties initParameters = deploymentConfiguration.getInitParameters();

        final String packagesToScan = initParameters.getProperty(PACKAGES_TO_SCAN_KEY);

        Reflections reflections;

        if (packagesToScan != null && !packagesToScan.isEmpty()) {
            reflections = new Reflections((Object[]) packagesToScan.split(","));
        } else {
            logger.warn("parameter 'ilay.packages_to_scan_for_visibility_engine' is not set, so all classes on the classpath will be scanned for visibility-annotations");
            reflections = new Reflections();
        }

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

    @Override
    public void uiInit(UIInitEvent event) {
        final UI ui = event.getUI();

        UiEventsHandler uiEventsHandler = new UiEventsHandler();

        ui.addAfterNavigationListener(uiEventsHandler);
        ui.addListener(AttachEvent.class, e -> uiEventsHandler.clearCache());
        ui.addListener(DetachEvent.class, e -> uiEventsHandler.removeComponentFromCache(e.getSource()));
    }

    private static class UiEventsHandler implements AfterNavigationListener {

        private final Map<Component, Map<Component, Annotation>> cache = new WeakHashMap<>();

        @Override
        public void afterNavigation(AfterNavigationEvent event) {
            event
                    .getActiveChain()
                    .stream()
                    .map(hasElement -> (Component) hasElement)
                    .map(component -> cache.computeIfAbsent(component, this::getComponentsToAnnotationsMap))
                    .forEach(map -> map.forEach(this::checkVisibility));
        }

        private Map<Component, Annotation> getComponentsToAnnotationsMap(Component component) {
            return component
                    .getChildren()
                    .map(this::resolveComponentsToAnnotations)
                    .map(Map::entrySet)
                    .flatMap(Set::stream)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        @SuppressWarnings("unchecked")
        private void checkVisibility(Component component, Annotation annotation) {

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

        @SuppressWarnings("unchecked")
        private Map<Component, Annotation> resolveComponentsToAnnotations(Component component) {
            final Class<? extends Component> componentClass = component.getClass();

            final Annotation annotationOnComponent = componentsToAnnotationsCache.get(componentClass);
            final Map<Field, Annotation> fieldAnnotationMap = fieldsToAnnotationCache.get(componentClass);

            final Map<Component, Annotation> mapFromFieldAnnotations;

            if (fieldAnnotationMap != null) {
                mapFromFieldAnnotations = fieldAnnotationMap
                        .entrySet()
                        .stream()
                        .collect(toMap(entry -> getComponentField(entry.getKey(), component), Map.Entry::getValue));
            } else {
                mapFromFieldAnnotations = null;
            }

            if (annotationOnComponent != null) {
                if (mapFromFieldAnnotations != null) {
                    mapFromFieldAnnotations.put(component, annotationOnComponent);
                } else {
                    return Collections.singletonMap(component, annotationOnComponent);
                }
            }

            return mapFromFieldAnnotations;
        }

        private Component getComponentField(Field field, Component component) {
            try {
                return (Component) field.get(component);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        void clearCache() {
            cache.clear();
        }

        void removeComponentFromCache(Component component) {
            cache.values().forEach(tupleSet -> tupleSet.remove(component));
        }
    }

}
