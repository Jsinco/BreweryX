package com.dre.brewery.files;

import eu.okaeri.configs.annotation.Exclude;
import lombok.Getter;
import lombok.Setter;

// I want to add support for an external recipes file along with BreweryX being able to read
// Recipes from the default config too. Gotta figure this out later
@Getter @Setter
public class Recipes extends AbstractOkaeriConfigFile {
    @Getter @Exclude
    private static final Recipes instance = createConfig(Recipes.class, "recipes.yml");
}
