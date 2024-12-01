package com.dre.brewery.integration;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.integration.barrel.WGBarrel;
import com.dre.brewery.integration.barrel.WGBarrel5;
import com.dre.brewery.integration.barrel.WGBarrel6;
import com.dre.brewery.integration.barrel.WGBarrel7;
import com.dre.brewery.utility.Logging;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;

@Getter
@Setter
public class WorldGuarkHook extends Hook {

    public static final WorldGuarkHook WORLDGUARD = new WorldGuarkHook("WorldGuard", config.isUseWorldGuard());

    private WGBarrel wgBarrel;


    public WorldGuarkHook(String name, boolean enabled) {
        super(name, enabled);

        if (!isEnabled()) {
            return;
        }

        Plugin plugin = WORLDGUARD.getPlugin();

        if (plugin == null) {
            Logging.errorLog("Failed loading WorldGuard Integration! Opening Barrels will NOT work!");
            Logging.errorLog("Brewery was tested with version 5.8, 6.1 and 7.0 of WorldGuard!");
            Logging.errorLog("Disable the WorldGuard support in the config and do /brew reload");
            return;
        }

        String wgv = plugin.getDescription().getVersion();
        if (wgv.startsWith("6.")) {
            wgBarrel = new WGBarrel6();
        } else if (wgv.startsWith("5.")) {
            wgBarrel = new WGBarrel5();
        } else {
            wgBarrel = new WGBarrel7();
        }
    }
}
