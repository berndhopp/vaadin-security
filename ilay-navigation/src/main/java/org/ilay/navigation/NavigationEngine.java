package org.ilay.navigation;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.ListenerPriority;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

import org.ilay.PermissionsChangedEvent;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static com.vaadin.flow.component.ComponentUtil.addListener;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * internally used class, do not touch
 */
@SuppressWarnings("unused")
@ListenerPriority(Integer.MAX_VALUE - 1)
public class NavigationEngine implements VaadinServiceInitListener, UIInitListener, BeforeEnterListener {

    private static final long serialVersionUID = 974589421761348380L;
    private final Map<Class<?>, Optional<AnnotationAccessEvaluatorTuple<?>>> cache = new ConcurrentHashMap<>();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event
                .getSource()
                .addUIInitListener(this);
    }

    @Override
    public void uiInit(UIInitEvent event) {
        final UI ui = event.getUI();

        ui.addBeforeEnterListener(this);

        addListener(ui, PermissionsChangedEvent.class, e -> ui.getPage().reload());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        checkAccessibility(event, event.getNavigationTarget());
    }

    @SuppressWarnings("unchecked")
    private <ANNOTATION extends Annotation> void checkAccessibility(BeforeEnterEvent event, Class<?> navigationTarget) {
        Optional<AnnotationAccessEvaluatorTuple<?>> optionalTuple = cache.computeIfAbsent(navigationTarget, this::getOptionalTuple);

        if (optionalTuple.isPresent()) {
            final AnnotationAccessEvaluatorTuple<ANNOTATION> tuple = (AnnotationAccessEvaluatorTuple<ANNOTATION>) optionalTuple.get();

            final AccessEvaluator<ANNOTATION> accessEvaluator = VaadinService
                    .getCurrent()
                    .getInstantiator()
                    .getOrCreate(tuple.getAccessEvaluatorClass());

            final Access access = requireNonNull(
                    accessEvaluator.evaluate(event.getLocation(), navigationTarget, tuple.getAnnotation()),
                    () -> tuple.getAccessEvaluatorClass() + "#checkAccess(BeforeEnterEvent) must not return null"
            );

            access.exec(event);
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<AnnotationAccessEvaluatorTuple<?>> getOptionalTuple(Class<?> classToCheck) {
        Predicate<Annotation> hasRestrictionAnnotation = annotation -> annotation
                .annotationType()
                .isAnnotationPresent(RestrictionAnnotation.class);

        List<Annotation> list = stream(classToCheck.getAnnotations())
                .filter(hasRestrictionAnnotation)
                .collect(toList());

        switch (list.size()) {
            case 0:
                return Optional.empty();
            case 1:
                final Annotation annotation = list.get(0);
                return Optional.of(new AnnotationAccessEvaluatorTuple(annotation, annotation.annotationType().getAnnotation(RestrictionAnnotation.class).value()));
            default:
                throw new IllegalStateException("more than one RestrictionAnnotation not allowed at " + classToCheck);
        }
    }

    private static class AnnotationAccessEvaluatorTuple<ANNOTATION extends Annotation> {

        private ANNOTATION annotation;
        private Class<? extends AccessEvaluator<ANNOTATION>> accessEvaluatorClass;

        AnnotationAccessEvaluatorTuple(ANNOTATION annotation, Class<? extends AccessEvaluator<ANNOTATION>> accessEvaluatorClass) {
            this.annotation = annotation;
            this.accessEvaluatorClass = accessEvaluatorClass;
        }

        ANNOTATION getAnnotation() {
            return annotation;
        }

        Class<? extends AccessEvaluator<ANNOTATION>> getAccessEvaluatorClass() {
            return accessEvaluatorClass;
        }
    }
}
