package com.dre.brewery.api.addons;

import com.dre.brewery.BreweryPlugin;

import java.util.logging.Level;

public class AddonLogger {

	private static final BreweryPlugin plugin = BreweryPlugin.getInstance();

	private final String addonName;
	private final String prefix;

	public AddonLogger(Class<? extends BreweryAddon> addonUninstantiated) {
		this.addonName = addonUninstantiated.getSimpleName();
		this.prefix = "&2[" + addonName + "] &r";
	}

	public void info(String message) {
		plugin.log(prefix + message);
	}

	public void warning(String message) {
		plugin.warningLog(prefix + message);
	}

	public void severe(String message) {
		plugin.errorLog(prefix + message);
	}

	public void info(String message, Throwable throwable) {
		info(message);
		plugin.getLogger().log(Level.INFO, "Stacktrace from " + addonName, throwable);
	}

	public void warning(String message, Throwable throwable) {
		warning(message);
		plugin.getLogger().log(Level.WARNING, "Stacktrace from " + addonName, throwable);
	}

	public void severe(String message, Throwable throwable) {
		severe(message);
		plugin.getLogger().log(Level.SEVERE, "Stacktrace from " + addonName, throwable);
	}
}
