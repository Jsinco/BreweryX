package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.annotation.LocalizedComment;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.sector.CustomItemsSector;
import com.dre.brewery.configuration.sector.capsule.ConfigCustomItem;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@OkaeriConfigFileOptions(value = "custom-items.yml", update = false)
@Setter
public class CustomItemsFile extends AbstractOkaeriConfigFile {

    @LocalizedComment("customitemsfile.header")
    private Map<String, ConfigCustomItem> customItems = new CustomItemsSector().getCapsules();

    // Backwards compatibility, merges custom items from the default config
    public Map<String, ConfigCustomItem> getCustomItems() {
        Map<String, ConfigCustomItem> map = new HashMap<>(this.customItems);
        map.putAll(ConfigManager.getConfig(Config.class).getCustomItems());
        return map;
    }
}
