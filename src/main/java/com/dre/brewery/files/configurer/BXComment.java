package com.dre.brewery.files.configurer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BXComment {
    /**
     * An array of all different versions of the comment to include.
     * @return the comment value
     */
    String[] value();
}
