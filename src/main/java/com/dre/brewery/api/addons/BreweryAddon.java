package com.dre.brewery.api.addons;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.CommandManager;
import com.dre.brewery.commands.SubCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public abstract class BreweryAddon {

	private final List<Listener> listeners = new ArrayList<>();
	private final List<String> commands = new ArrayList<>();
	private AddonLogger logger = null;
	private AddonFileManager addonFileManager = null;
	private AddonInfo addonInfo = null;
	private AddonManager addonManager = null;

	public void onAddonPreEnable() {
	}

	public void onAddonEnable() {
	}

	public void onAddonDisable() {
	}

	public void onBreweryReload() {
	}

	@NotNull
	public BreweryPlugin getBreweryPlugin() {
		return BreweryPlugin.getInstance();
	}

	@NotNull
	public AddonFileManager getAddonFileManager() {
		return addonFileManager;
	}

	@NotNull
	public AddonLogger getAddonLogger() {
		return logger;
	}

	@NotNull
	public AddonInfo getAddonInfo() {
		return addonInfo;
	}

	@NotNull
	public AddonManager getAddonManager() {
		return addonManager;
	}

	public void registerListener(Listener listener) {
		getBreweryPlugin().getServer().getPluginManager().registerEvents(listener, getBreweryPlugin());
		listeners.add(listener);
	}

	public void unregisterListener(Listener listener) {
		HandlerList.unregisterAll(listener);
		listeners.remove(listener);
	}


	public void registerCommand(String name, SubCommand command) {
		CommandManager.addSubCommand(name, command);
		commands.add(name);
	}

	public void unregisterCommand(String name) {
		CommandManager.removeSubCommand(name);
		commands.remove(name);
	}

	public void unregisterListeners() {
		for (Listener listener : listeners) {
			HandlerList.unregisterAll(listener);
		}
		listeners.clear();
	}

	public void unregisterCommands() {
		for (String command : commands) {
			CommandManager.removeSubCommand(command);
		}
		commands.clear();
	}
}
