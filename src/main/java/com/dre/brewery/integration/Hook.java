package com.dre.brewery.integration;

import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.files.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@Getter
@Setter
@AllArgsConstructor
public class Hook {

    protected static final Config config = ConfigManager.getConfig(Config.class);

    public static final Hook LWC = new Hook("LWC", config.isUseLWC());
    public static final Hook GRIEFPREVENTION = new Hook("GriefPrevention", config.isUseGriefPrevention());
    public static final Hook TOWNY = new Hook("Towny", config.isUseTowny());
    public static final Hook BLOCKLOCKER = new Hook("BlockLocker", config.isUseBlockLocker());
    public static final Hook GAMEMODEINVENTORIES = new Hook("GameModeInventories", config.isUseGMInventories());
    public static final Hook MMOITEMS = new Hook("MMOItems");
    public static final Hook VAULT = new Hook("Vault");
    public static final Hook CHESTSHOP = new Hook("ChestShop");
    public static final Hook SHOPKEEPERS = new Hook("ShopKeepers");
    public static final Hook SLIMEFUN = new Hook("Slimefun");
    public static final Hook ORAXEN = new Hook("Oraxen");
    public static final Hook ITEMSADDER = new Hook("ItemsAdder");

    protected final String name;
    protected boolean enabled;
    protected boolean checked;

    public Hook(String name) {
        this.name = name;
        this.enabled = true;
    }

    public Hook(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        if (!checked) {
            checked = true;
            if (enabled) {
                enabled = Bukkit.getPluginManager().isPluginEnabled(name);
            }
        }
        return enabled;
    }

    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(name);
    }

}
