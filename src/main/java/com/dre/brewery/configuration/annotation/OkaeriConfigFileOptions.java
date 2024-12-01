package com.dre.brewery.configuration.annotation;

import com.dre.brewery.configuration.configurer.BreweryXConfigurer;
import eu.okaeri.configs.configurer.Configurer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OkaeriConfigFileOptions {

    /**
     * The name of the file to be created
     * @return the name of the file
     */
    String value() default "";

    /**
     * Uses the name of our lang file for this class' bind file.
     * @see com.dre.brewery.configuration.configurer.Translation
     * @see com.dre.brewery.configuration.configurer.TranslationManager
     * @return true if the lang file name should be used
     */
    boolean useLangFileName() default false;

    /**
     * Should the file be updated by Okaeri or read-only
     * @return true if the file should be updated
     */
    boolean update() default true;

    /**
     * Remove orphan keys?
     * @return true if orphan keys should be removed
     */
    boolean removeOrphans() default false;

    /**
     * The configurer to be used for the file
     * @return the configurer class
     */
    Class<? extends Configurer> configurer() default BreweryXConfigurer.class;
}
