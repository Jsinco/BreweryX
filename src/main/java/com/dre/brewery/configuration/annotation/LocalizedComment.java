package com.dre.brewery.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for declaring comments which change based on the selected language.
 * @see com.dre.brewery.configuration.configurer.BreweryXConfigurer
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalizedComment {
	String[] value();
}
