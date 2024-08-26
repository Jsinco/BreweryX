package com.dre.brewery.api.addons;

import com.dre.brewery.BreweryPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BreweryAddon {

	private AddonLogger logger = null;
	private AddonFileManager addonFileManager = null;
	private AddonInfo addonInfo = null;


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

	@Nullable
	public AddonInfo getAddonInfo() {
		return addonInfo;
	}
}
