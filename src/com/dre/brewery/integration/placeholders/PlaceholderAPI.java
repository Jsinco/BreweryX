package com.dre.brewery.integration.placeholders;

import com.dre.brewery.BPlayer;
import com.dre.brewery.utility.BUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPI extends PlaceholderExpansion {
	@Override
	public @NotNull String getIdentifier() {
		return "breweryx";
	}
	@Override
	public @NotNull String getAuthor() {
		return "BreweryX";
	}
	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}
	@Override
	public String onRequest(OfflinePlayer player, @NotNull String params) {
		BPlayer bPlayer = BPlayer.get(player);
		if (bPlayer == null) bPlayer = new BPlayer(BUtil.playerString(player));
		if (params.equalsIgnoreCase("drunkenness")) return String.valueOf(bPlayer.getDrunkeness());
		if (params.equalsIgnoreCase("drunkenness_bars")) return bPlayer.generateBars();
		if (params.equalsIgnoreCase("quality")) return String.valueOf(bPlayer.getQuality());
		if (params.equalsIgnoreCase("quality_stars")) return bPlayer.generateStars();
		return null;
	}
}
