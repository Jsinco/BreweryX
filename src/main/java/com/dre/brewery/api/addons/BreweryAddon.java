/*
 * BreweryX Bukkit-Plugin for an alternate brewing process
 * Copyright (C) 2024 The Brewery Team
 *
 * This file is part of BreweryX.
 *
 * BreweryX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BreweryX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BreweryX. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package com.dre.brewery.api.addons;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.CommandManager;
import com.dre.brewery.commands.SubCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class BreweryAddon {

	private final List<Listener> listeners = new ArrayList<>();
	private final List<String> commands = new ArrayList<>();

	private AddonInfo addonInfo;

	private AddonLogger logger;
	private AddonFileManager addonFileManager;
	private AddonConfigManager addonConfigManager;
	private AddonManager addonManager;

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
	public AddonInfo getAddonInfo() {
		return addonInfo;
	}

	@NotNull
	public AddonFileManager getAddonFileManager() {
		return addonFileManager;
	}

	@NotNull
	public AddonConfigManager getAddonConfigManager() {
		return addonConfigManager;
	}

	@NotNull
	public AddonLogger getAddonLogger() {
		return logger;
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
