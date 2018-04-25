package org.ilay;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.ListenerPriority;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * internally used class, do not touch
 */
@SuppressWarnings("unused")
@ListenerPriority(Integer.MAX_VALUE - 1)
public class IlayEngine implements VaadinServiceInitListener, UIInitListener, BeforeEnterListener {

    private static final long serialVersionUID = 974589421761348380L;
    private final Map<Class<?>, Optional<Annotation>> cache = new ConcurrentHashMap<>();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event
                .getSource()
                .addUIInitListener(this);
    }

    @Override
    public void uiInit(UIInitEvent event) {
        event
                .getUI()
                .addBeforeEnterListener(this);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Class<?> navigationTarget = event.getNavigationTarget();

        Optional<Annotation> optionalAnnotation = cache.computeIfAbsent(
                navigationTarget,
                nt -> {
                    Predicate<Annotation> hasRestrictionAnnotation = annotation -> annotation
                            .annotationType()
                            .isAnnotationPresent(RestrictionAnnotation.class);

                    List<Annotation> list = stream(nt.getAnnotations())
                            .filter(hasRestrictionAnnotation)
                            .collect(toList());

                    switch (list.size()) {
                        case 0:
                            return Optional.empty();
                        case 1:
                            return Optional.of(list.get(0));
                        default:
                            throw new IllegalStateException("more than one RestrictionAnnotation not allowed");
                    }
                }
        );

        optionalAnnotation.ifPresent(annotation -> {
                    RestrictionAnnotation restrictionAnnotation = annotation.annotationType().getAnnotation(RestrictionAnnotation.class);

                    final Class<? extends AccessEvaluator> accessEvaluatorType = restrictionAnnotation.value();

                    final AccessEvaluator accessEvaluator = VaadinService
                            .getCurrent()
                            .getInstantiator()
                            .getOrCreate(accessEvaluatorType);

                    final Access access = requireNonNull(
                            accessEvaluator.evaluate(event.getLocation(), navigationTarget, annotation),
                            () -> accessEvaluatorType + "#checkAccess(BeforeEnterEvent) must not return null"
                    );

                    access.exec(event);
                }
        );
    }
}
