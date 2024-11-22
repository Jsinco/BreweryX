package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.annotation.LocalizedComment;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.sector.CauldronSector;
import com.dre.brewery.configuration.sector.capsule.ConfigCauldronIngredient;
import eu.okaeri.configs.annotation.Header;

import java.util.HashMap;
import java.util.Map;

@Header("!!! IMPORTANT: BreweryX configuration files do NOT support external comments! If you add any comments, they will be overwritten !!!")
@OkaeriConfigFileOptions("cauldron.yml")
public class CauldronFile extends AbstractOkaeriConfigFile {

    @LocalizedComment("cauldronFile.header")
    private Map<String, ConfigCauldronIngredient> cauldron = new CauldronSector().getCapsules();

    // Backwards compatibility, merges cauldron ingredients from the default config
    public Map<String, ConfigCauldronIngredient> getCauldronIngredients() {
        Map<String, ConfigCauldronIngredient> map = new HashMap<>(this.cauldron);
        map.putAll(ConfigManager.getConfig(Config.class).getCauldron());
        return map;
    }

    // Better setter name
    public void setCauldronIngredients(Map<String, ConfigCauldronIngredient> cauldron) {
        this.cauldron = cauldron;
    }
}
