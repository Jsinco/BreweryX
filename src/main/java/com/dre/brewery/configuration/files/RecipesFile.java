package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.annotation.Footer;
import com.dre.brewery.configuration.annotation.LocalizedComment;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.sector.RecipesSector;
import com.dre.brewery.configuration.sector.capsule.ConfigRecipe;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Footer({
	"More recipe ideas:",
	"Dandelion Liquor",
	"Beetroot Spirit",
	"Poppy Liquor: Macum/Grand Poppy",
	"Bamboo Liquor: Chu Yeh Ching",
	"Cachaca",
	"Cognac",
	"Sake",
	"Buorbon",
	"Moonshine",
	"Different Wines",
	"Brandy",
	"Amaretto",
	"etc. as well as variations like,",
	"Pumpkin Spice Beer",
	"Melon Vodka",
	"",
	"There are a lot of items in Minecraft like Vines, Milk and items added by plugins that would make great ingredients."
})
@OkaeriConfigFileOptions(value = "recipes.yml", update = false)
@Setter
public class RecipesFile extends AbstractOkaeriConfigFile {


    @LocalizedComment("recipesFile.header")
    private Map<String, ConfigRecipe> recipes = new RecipesSector().getCapsules();

    // Backwards compatibility, merges recipes from the default config
    public Map<String, ConfigRecipe> getRecipes() {
        Map<String, ConfigRecipe> map = new HashMap<>(this.recipes);
        map.putAll(ConfigManager.getConfig(Config.class).getRecipes());
        return map;
    }
}
