package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.annotation.LocalizedComment;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.sector.RecipesSector;
import com.dre.brewery.configuration.sector.capsule.ConfigRecipe;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@OkaeriConfigFileOptions(value = "recipes.yml", update = false)
@Setter
public class RecipesFile extends AbstractOkaeriConfigFile {


    @LocalizedComment("recipesfile.header")
    private Map<String, ConfigRecipe> recipes = new RecipesSector().getCapsules();

    // Backwards compatibility, merges recipes from the default config
    public Map<String, ConfigRecipe> getRecipes() {
        Map<String, ConfigRecipe> map = new HashMap<>(this.recipes);
        map.putAll(ConfigManager.getConfig(Config.class).getRecipes());
        return map;
    }
}
