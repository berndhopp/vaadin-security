package org.ilay;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.server.VaadinService;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public class IlayUI extends UI {

    private static final long serialVersionUID = 974589421761348380L;

    private static final Map<Class<?>, Optional<Annotation>> cache = Collections.synchronizedMap(new WeakHashMap<>());

    @SuppressWarnings("unchecked")
    public IlayUI() {
        addBeforeEnterListener(event -> {
            final Class<?> navigationTarget = event.getNavigationTarget();

            Optional<Annotation> optionalAnnotation = cache.computeIfAbsent(navigationTarget, nt -> Arrays.stream(nt.getAnnotations())
                    .filter(annotation -> annotation.annotationType().isAnnotationPresent(RestrictionAnnotation.class))
                    .findAny());

            optionalAnnotation.ifPresent(annotation -> {
                RestrictionAnnotation restrictionAnnotation = annotation.annotationType().getAnnotation(RestrictionAnnotation.class);

                final VaadinService vaadinService = requireNonNull(VaadinService.getCurrent());

                final Instantiator instantiator = requireNonNull(vaadinService.getInstantiator());

                final AccessEvaluator accessEvaluator = requireNonNull(instantiator.getOrCreate(restrictionAnnotation.value()));

                final Access access = requireNonNull(
                        accessEvaluator.evaluate(event.getLocation(), navigationTarget, annotation),
                        () -> restrictionAnnotation.value() + "#checkAccess(BeforeEnterEvent) must not return null"
                );

                access.exec(event);
            });

        });
    }
}
