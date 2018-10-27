package org.ilay.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Import(PostProcessor.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableIlay {
}
