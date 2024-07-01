package com.dre.brewery.integration.papi;

import com.dre.brewery.BPlayer;
import com.dre.brewery.BreweryPlugin;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Placeholder {
    @Nullable
    String onReceivedRequest(BreweryPlugin plugin, OfflinePlayer player, BPlayer bPlayer, String[] args);
}
