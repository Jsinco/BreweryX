package com.dre.brewery.files.configurer;

import com.dre.brewery.files.AbstractOkaeriConfigFile;
import eu.okaeri.configs.annotation.Exclude;
import lombok.Getter;
import lombok.Setter;

// Example config class showing BXComment
@Getter @Setter
public class ExampleConfigClass extends AbstractOkaeriConfigFile {

    @Exclude @Getter
    private static final ExampleConfigClass instance = createConfig(ExampleConfigClass.class, "example-config.yml", new BreweryXConfigurer());

    @BXComment(
            {"""
            This is a comment in english""",
            """
            This is a comment in german""",
            """
            This is a comment in spanish"""
            })
    private String exampleString = "Hello, World!";

}
