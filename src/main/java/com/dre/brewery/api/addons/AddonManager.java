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
import com.dre.brewery.utility.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;

public class AddonManager extends ClassLoader {

	private final BreweryPlugin plugin;
	private final File addonsFolder;
	private final static List<BreweryAddon> addons = new ArrayList<>();
	private final static List<AddonCommand> addonCommands = new ArrayList<>();

	public AddonManager(BreweryPlugin plugin) {
        this.plugin = plugin;
		this.addonsFolder = new File(plugin.getDataFolder(), "addons");
		if (!addonsFolder.exists()) {
			addonsFolder.mkdirs();
		}
	}

	public void unloadAddons() {
		for (BreweryAddon addon : addons) {
			try {
				addon.onAddonDisable();
				addon.unregisterListeners();
				addon.unregisterCommands();
			} catch (Throwable t) {
				Logging.errorLog("Failed to disable addon " + addon.getClass().getSimpleName(), t);
			}
		}
		int loaded = addons.size();
		if (loaded > 0) Logging.log("Disabled " + loaded + " addon(s)");
		addons.clear();
	}

	public void unloadAddon(BreweryAddon addon) {
		try {
			addon.onAddonDisable();
			addon.unregisterListeners();
			addon.unregisterCommands();
		} catch (Throwable t) {
			Logging.errorLog("Failed to disable addon " + addon.getClass().getSimpleName(), t);
		}
		addons.remove(addon);
	}


	public void reloadAddons() {
		for (BreweryAddon addon : addons) {
			try {
				addon.onBreweryReload();
			} catch (Throwable t) {
				Logging.errorLog("Failed to reload addon " + addon.getClass().getSimpleName(), t);
			}
		}
		int loaded = addons.size();
		if (loaded > 0) Logging.log("Reloaded " + loaded + " addon(s)");
	}

	public List<BreweryAddon> getAddons() {
		return addons;
	}

	/**
	 * Get all classes that extend Addon and instantiates them
	 */
	public void loadAddons() {
		File[] files = addonsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
		if (files == null) {
			return;
		}

		for (File file : files) {
			loadAddon(file);
		}

		for (BreweryAddon addon : addons) {
			try {
				addon.onAddonEnable();
			} catch (Throwable t) {
				Logging.errorLog("Failed to enable addon " + addon.getClass().getSimpleName(), t);
			}
		}
		int loaded = addons.size();
		if (loaded > 0) Logging.log("Loaded " + loaded + " addon(s)");
	}


	public void loadAddon(File file) {
		try {
			List<Class<?>> classes = loadAllClassesFromJar(file);

			for (Class<?> clazz : classes) {
				if (!BreweryAddon.class.isAssignableFrom(clazz)) {
					continue;
				}
				Class<? extends BreweryAddon> addonClass = clazz.asSubclass(BreweryAddon.class);
				try {
					BreweryAddon addon = addonClass.getConstructor().newInstance();
					Class<BreweryAddon> breweryAddonClass = BreweryAddon.class;
					// Set the logger and file manager
					Field infoField = breweryAddonClass.getDeclaredField("addonInfo"); infoField.setAccessible(true);
					infoField.set(addon, addonClass.getAnnotation(AddonInfo.class));

					if (addon.getAddonInfo() == null) { // This CAN be null for us. It's only annotated NotNull for addons.
						Logging.errorLog("Addon " + addonClass.getSimpleName() + " is missing the AddonInfo annotation. It will not be loaded.");
						continue;
					}

					Field loggerField = breweryAddonClass.getDeclaredField("logger"); loggerField.setAccessible(true);
					Field fileManagerField = breweryAddonClass.getDeclaredField("addonFileManager"); fileManagerField.setAccessible(true);
					Field addonConfigManagerField = breweryAddonClass.getDeclaredField("addonConfigManager"); addonConfigManagerField.setAccessible(true);
					Field managerField = breweryAddonClass.getDeclaredField("addonManager"); managerField.setAccessible(true);


					loggerField.set(addon, new AddonLogger(addonClass));
					fileManagerField.set(addon, new AddonFileManager(addon, file));
					addonConfigManagerField.set(addon, new AddonConfigManager(addon));
					managerField.set(addon, this);


					addon.getAddonLogger().info("Loading &a" + addon.getAddonInfo().name() + " &f-&a v" + addon.getAddonInfo().version() + " &fby &a" + addon.getAddonInfo().author());
					addons.add(addon); // Add to our list of addons

					// let the addon know it has been enabled
					addon.onAddonPreEnable();
				} catch (Exception e) {
					Logging.errorLog("Failed to load addon: " + file.getName(), e);
				}
			}
		} catch (Throwable ex) {
			Logging.errorLog("Failed to load addon classes from jar " + file.getName(), ex);
		}
	}


	private static <T> @NotNull List<Class<? extends T>> findClasses(@NotNull final File file, @NotNull final Class<T> clazz) throws CompletionException {
		if (!file.exists()) {
			return Collections.emptyList();
		}

		final List<Class<? extends T>> classes = new ArrayList<>();

		final List<String> matches = matchingNames(file);

		for (final String match : matches) {
			try {
				final URL jar = file.toURI().toURL();
				try (final URLClassLoader loader = new URLClassLoader(new URL[]{jar}, clazz.getClassLoader())) {
					Class<? extends T> addonClass = loadClass(loader, match, clazz);
					if (addonClass != null) {
						classes.add(addonClass);
					}
				}
			} catch (final VerifyError ignored) {
			} catch (IOException | ClassNotFoundException e) {
				throw new CompletionException(e.getCause());
			}
		}
		return classes;
	}

	private List<Class<?>> loadAllClassesFromJar(File jarFile) {
		List<Class<?>> classes = new ArrayList<>();
		try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, getClass().getClassLoader())) {

			try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile))) {
				JarEntry jarEntry;
				String mainDir = "";
				while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
					if (jarEntry.getName().endsWith(".class")) {
						String className = jarEntry.getName().replaceAll("/", ".").replace(".class", "");
						try {
							Class<?> clazz;
							try {
								clazz = Class.forName(className, false, classLoader);
							} catch (ClassNotFoundException | NoClassDefFoundError e) {
								continue;
							}
                            if (BreweryAddon.class.isAssignableFrom(clazz)) {
								classLoader.loadClass(className);
								mainDir = className.substring(0, className.lastIndexOf('.'));
							}
							if (!clazz.getName().contains(mainDir)) {
								continue;
							}
							classes.add(clazz);

						} catch (ClassNotFoundException e) {
							plugin.getLogger().log(Level.SEVERE, "Failed to load class " + className, e);
						}
					}
				}
				for (Class<?> clazz : classes) {
					if (!BreweryAddon.class.isAssignableFrom(clazz)) {
						try {
							classLoader.loadClass(clazz.getName());
						} catch (ClassNotFoundException e) {
							plugin.getLogger().log(Level.SEVERE, "Failed to load class " + clazz.getName(), e);
						}
					}
				}
			}
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Error loading classes from JAR", e);
		}
		return classes;
	}

	private static @NotNull List<String> matchingNames(final File file) {
		final List<String> matches = new ArrayList<>();
		try {
			final URL jar = file.toURI().toURL();
			try (final JarInputStream stream = new JarInputStream(jar.openStream())) {
				JarEntry entry;
				while ((entry = stream.getNextJarEntry()) != null) {
					final String name = entry.getName();
					if (!name.endsWith(".class")) {
						continue;
					}

					matches.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
				}
			}
		} catch (Exception e) {
			return Collections.emptyList();
		}
		return matches;
	}

	private static <T> @Nullable Class<? extends T> loadClass(final @NotNull URLClassLoader loader, final String match, @NotNull final Class<T> clazz) throws ClassNotFoundException {
		try {
			final Class<?> loaded = loader.loadClass(match);
			if (clazz.isAssignableFrom(loaded)) {
				return (loaded.asSubclass(clazz));
			}
		} catch (final NoClassDefFoundError ignored) {
		}
		return null;
	}

}
