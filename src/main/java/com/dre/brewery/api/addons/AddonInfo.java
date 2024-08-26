package com.dre.brewery.api.addons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddonInfo {
	String author() default "Unknown";
	String version() default "0-BETA";
	String description() default "";
	String name() default "";
}
