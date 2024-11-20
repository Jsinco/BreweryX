package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.annotation.LocalizedComment;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.sector.CustomItemsSector;
import com.dre.brewery.configuration.sector.capsule.ConfigCustomItem;
import com.dre.brewery.recipe.RecipeItem;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OkaeriConfigFileOptions(value = "custom-items.yml", update = false)
@Setter
public class CustomItemsFile extends AbstractOkaeriConfigFile {

    @LocalizedComment("customItemsFile.header")
    private Map<String, ConfigCustomItem> customItems = new CustomItemsSector().getCapsules();

    // Backwards compatibility, merges custom items from the default config
    public Map<String, ConfigCustomItem> getCustomItems() {
        Map<String, ConfigCustomItem> map = new HashMap<>(this.customItems);
        map.putAll(ConfigManager.getConfig(Config.class).getCustomItems());
        return map;
    }

    public List<RecipeItem> getRecipeItems() {
        List<RecipeItem> recipeItems = new ArrayList<>();

        for (var entry : getCustomItems().entrySet()) {
            ConfigCustomItem customItem = entry.getValue();
            recipeItems.add(RecipeItem.fromConfigCustom(entry.getKey(), customItem));
        }
        return recipeItems;
    }
}
