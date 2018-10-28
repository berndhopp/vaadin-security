package org.ilay.visibility;

import com.google.inject.Binding;
import com.google.inject.matcher.AbstractMatcher;

import static org.ilay.visibility.VisibilityUtil.hasVisibilityAnnotation;

class VisibilityAnnotationMatcher extends AbstractMatcher<Binding<?>> {
    public boolean matches(Binding<?> binding) {
        return hasVisibilityAnnotation(binding.getKey().getTypeLiteral().getRawType());
    }
}
