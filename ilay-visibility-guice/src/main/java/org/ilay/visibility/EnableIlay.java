package org.ilay.visibility;

import com.vaadin.guice.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Import(IlayModule.class)
public @interface EnableIlay {
}
