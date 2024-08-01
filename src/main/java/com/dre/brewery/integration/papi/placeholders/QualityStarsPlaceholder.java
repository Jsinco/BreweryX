package com.dre.brewery.integration.papi.placeholders;

import com.dre.brewery.BPlayer;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.integration.papi.Placeholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

public class QualityStarsPlaceholder implements Placeholder {
    @Override
    public @Nullable String onReceivedRequest(BreweryPlugin plugin, OfflinePlayer player, BPlayer bPlayer, String[] args) {
        return bPlayer.generateStars();
    }
}
