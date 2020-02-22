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

    @SuppressWarnings("unchecked")
    private static final Class<? extends AccessEvaluator>[] EMPTY = new Class[]{};

    private static final long serialVersionUID = 974589421761348380L;
    private final Map<Class<?>, Class<? extends AccessEvaluator>[]> cache = new ConcurrentHashMap<>();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event
                .getSource()
                .addUIInitListener(this);
    }

    @Override
    public void uiInit(UIInitEvent event) {
        event.getUI().addBeforeEnterListener(this);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Class<? extends AccessEvaluator>[] optionalTuple = cache.computeIfAbsent(event.getNavigationTarget(), this::getAccessEvaluators);

        for (Class<? extends AccessEvaluator> aClass : optionalTuple) {
            final AccessEvaluator accessEvaluator = VaadinService
                    .getCurrent()
                    .getInstantiator()
                    .getOrCreate(aClass);

            final Access access = requireNonNull(
                    accessEvaluator.evaluate(event),
                    () -> aClass + "#checkAccess(BeforeEnterEvent) must not return null"
            );

            access.exec(event);

            if(!access.isGranted()){
                return;
            }
        }
    }

    private Class<? extends AccessEvaluator>[] getAccessEvaluators(Class<?> classToCheck) {
        Predicate<Annotation> hasRestrictionAnnotation = annotation -> annotation
                .annotationType()
                .isAnnotationPresent(NavigationAnnotation.class);

        List<Annotation> list = stream(classToCheck.getAnnotations())
                .filter(hasRestrictionAnnotation)
                .collect(toList());

        switch (list.size()) {
            case 0:
                return EMPTY;
            case 1:
                final Annotation annotation = list.get(0);
                return annotation.annotationType().getAnnotation(NavigationAnnotation.class).value();
            default:
                throw new IllegalStateException("more than one NavigationAnnotation not allowed at " + classToCheck);
        }
    }
}
