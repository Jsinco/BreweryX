package com.dre.brewery.integration;

import com.dre.brewery.integration.papi.PlaceholderAPIManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends Hook {

    public static final PlaceholderAPIHook PLACEHOLDERAPI = new PlaceholderAPIHook("PlaceholderAPI");

    private PlaceholderAPIManager instance;

    public PlaceholderAPIHook(String name) {
        super(name);
    }

    public PlaceholderAPIManager getInstance() {
        if (instance == null && isEnabled()) {
            instance = new PlaceholderAPIManager();
        }
        return instance;
    }

    public String setPlaceholders(@Nullable Player player, String text) {
        if (!this.isEnabled()) {
            return text;
        }

        return PlaceholderAPI.setPlaceholders(player, text);
    }

}
