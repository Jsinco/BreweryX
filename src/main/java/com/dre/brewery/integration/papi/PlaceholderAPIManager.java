package com.dre.brewery.integration.papi;

import com.dre.brewery.BPlayer;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.integration.papi.placeholders.DrunkennessBarsPlaceholder;
import com.dre.brewery.integration.papi.placeholders.DrunkennessPlaceholder;
import com.dre.brewery.integration.papi.placeholders.QualityPlaceholder;
import com.dre.brewery.integration.papi.placeholders.QualityStarsPlaceholder;
import com.dre.brewery.utility.BUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderAPIManager extends PlaceholderExpansion {

	private static final BreweryPlugin plugin = BreweryPlugin.getInstance();
	private static final Map<String, Placeholder> placeholders = new HashMap<>();

	public PlaceholderAPIManager() {
		placeholders.put("drunkenness", new DrunkennessPlaceholder());
		placeholders.put("drunkennessbars", new DrunkennessBarsPlaceholder());
		placeholders.put("quality", new QualityPlaceholder());
		placeholders.put("qualitystars", new QualityStarsPlaceholder());
	}

	@Override
	public @NotNull String getIdentifier() {
		return "breweryx";
	}

	@Override
	public @NotNull String getAuthor() {
		return "Mitality & Jsinco";
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onRequest(OfflinePlayer player, @NotNull String params) {
		BPlayer bPlayer = BPlayer.get(player);
		if (bPlayer == null) bPlayer = new BPlayer(BUtil.playerString(player));

		String[] args = params.split("_");

		Placeholder placeholder = placeholders.get(args[0].toLowerCase());
		if (placeholder != null) {
			return placeholder.onReceivedRequest(plugin, player, bPlayer, args);
		}
		return null;
	}
}
