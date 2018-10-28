package org.ilay.visibility;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;

import org.ilay.PermissionsChangedEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import static com.vaadin.flow.component.ComponentUtil.addListener;
import static org.ilay.visibility.VisibilityUtil.evaluateVisibility;
import static org.ilay.visibility.VisibilityUtil.hasVisibilityAnnotation;

@org.springframework.stereotype.Component
class PostProcessor implements BeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if (!(bean instanceof Component)) {
            return bean;
        }

        Component component = (Component) bean;

        if (hasVisibilityAnnotation(component.getClass())) {
            addListener(UI.getCurrent(), PermissionsChangedEvent.class, e -> evaluateVisibility(component));
            evaluateVisibility(component);
        }

        return component;
    }
}
