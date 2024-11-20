/**
 *
 *     Brewery Minecraft-Plugin for an alternate Brewing Process
 *     Copyright (C) 2021 Milan Albrecht
 *
 *     This file is part of Brewery.
 *
 *     Brewery is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Brewery is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Brewery.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.dre.brewery;

import com.dre.brewery.api.addons.AddonManager;
import com.dre.brewery.commands.CommandManager;
import com.dre.brewery.commands.subcommands.ReloadCommand;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.integration.Hook;
import com.dre.brewery.configuration.configurer.TranslationManager;
import com.dre.brewery.configuration.files.Lang;
import com.dre.brewery.utility.UpdateChecker;
import com.dre.brewery.configuration.files.Config;
import com.dre.brewery.integration.listeners.ChestShopListener;
import com.dre.brewery.integration.listeners.IntegrationListener;
import com.dre.brewery.integration.listeners.ShopKeepersListener;
import com.dre.brewery.integration.listeners.SlimefunListener;
import com.dre.brewery.integration.barrel.BlocklockerBarrel;
import com.dre.brewery.integration.papi.PlaceholderAPI;
import com.dre.brewery.listeners.BlockListener;
import com.dre.brewery.listeners.CauldronListener;
import com.dre.brewery.listeners.EntityListener;
import com.dre.brewery.listeners.InventoryListener;
import com.dre.brewery.listeners.PlayerListener;
import com.dre.brewery.recipe.CustomItem;
import com.dre.brewery.recipe.Ingredient;
import com.dre.brewery.recipe.ItemLoader;
import com.dre.brewery.recipe.PluginItem;
import com.dre.brewery.recipe.SimpleItem;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.storage.StorageInitException;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.LegacyUtil;
import com.dre.brewery.utility.MinecraftVersion;
import com.dre.brewery.integration.bstats.Stats;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BreweryPlugin extends JavaPlugin {

	private static final int RESOURCE_ID = 114777;

	private static AddonManager addonManager;
	private static TaskScheduler scheduler;
	private static BreweryPlugin breweryPlugin;
	private static MinecraftVersion minecraftVersion;
	private static DataManager dataManager;
	private static boolean isFolia = false;
	public static boolean useNBT;


	public PlayerListener playerListener; // Public Listeners
	public Map<String, Function<ItemLoader, Ingredient>> ingredientLoaders = new HashMap<>(); // Registrations
	public Stats stats; // Metrics


	@Override
	public void onLoad() {
		breweryPlugin = this;
		minecraftVersion = MinecraftVersion.getIt();
		scheduler = UniversalScheduler.getScheduler(this);

		// Needs to be here otherwise BreweryX can't get the right language before Okaeri starts loading.
		TranslationManager.newInstance(this.getDataFolder());
	}

	@Override
	public void onEnable() {
		migrateBreweryDataFolder();
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			isFolia = true;
		} catch (ClassNotFoundException ignored) {
		}


		// MC 1.13 uses a different NBT API than the newer versions.
		// We decide here which to use, the new or the old or none at all
		if (LegacyUtil.initNbt()) {
			useNBT = true;
		}

		if (getMCVersion().isOrLater(MinecraftVersion.V1_14)) {
			// Campfires are weird
			// Initialize once now so it doesn't lag later when we check for campfires under Cauldrons
			getServer().createBlockData(Material.CAMPFIRE);
		}


		// TODO: cleanup with configs here

		// Register Item Loaders
		CustomItem.registerItemLoader(this);
		SimpleItem.registerItemLoader(this);
		PluginItem.registerItemLoader(this);

		// Load config and lang
		Config config = ConfigManager.getConfig(Config.class);
		Lang lang = ConfigManager.getConfig(Lang.class);
		config.load();
		config.save();


		BSealer.registerRecipe(); // Sealing table recipe
		ConfigManager.loadRecipes();
		ConfigManager.loadCauldronIngredients();
		ConfigManager.loadDistortWords();
		ConfigManager.registerDefaultPluginItems(); // Register plugin items
		this.stats = new Stats(); // Load metrics

        // Load Addons
		addonManager = new AddonManager(this);
		addonManager.loadAddons();



		log("Minecraft version&7:&a " + minecraftVersion.getVersion());
		if (minecraftVersion == MinecraftVersion.UNKNOWN) {
			warningLog("This version of Minecraft is not known to Brewery! Please be wary of bugs or other issues that may occur in this version.");
		}


		// Load DataManager
        try {
            dataManager = DataManager.createDataManager(config.getStorage());
        } catch (StorageInitException e) {
            errorLog("Failed to initialize DataManager!", e);
			Bukkit.getPluginManager().disablePlugin(this);
        }

		DataManager.loadMiscData(dataManager.getBreweryMiscData());
		Barrel.getBarrels().addAll(dataManager.getAllBarrels());
		// Stream error? - https://gist.github.com/TomLewis/413212bd3df6cb745412475128e01e92w
		// Apparently there's 2 CraftBlocks trying to be put under the same identifier in the map and it's throwing an err
		// I'll fix the stream issues in the next version but I have to release this fix ASAP so I'm leaving it like this for now. - Jsinco

		/*
		BCauldron.getBcauldrons().putAll(dataManager.getAllCauldrons().stream().collect(Collectors.toMap(BCauldron::getBlock, Function.identity())));
		BPlayer.getPlayers().putAll(dataManager.getAllPlayers().stream().collect(Collectors.toMap(BPlayer::getUuid, Function.identity())));
		 */
		for (BCauldron cauldron : dataManager.getAllCauldrons()) {
			BCauldron.getBcauldrons().put(cauldron.getBlock(), cauldron);
		}
		for (BPlayer player : dataManager.getAllPlayers()) {
			BPlayer.getPlayers().put(player.getUuid(), player);
		}
		Wakeup.getWakeups().addAll(dataManager.getAllWakeups());


		// Setup Metrics
		stats.setupBStats();


		getCommand("breweryx").setExecutor(new CommandManager());
		// Listeners
		playerListener = new PlayerListener();

		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getServer().getPluginManager().registerEvents(new IntegrationListener(), this);
		if (getMCVersion().isOrLater(MinecraftVersion.V1_9)) {
			getServer().getPluginManager().registerEvents(new CauldronListener(), this);
		}
		if (Hook.CHESTSHOP.isEnabled() && getMCVersion().isOrLater(MinecraftVersion.V1_13)) {
			getServer().getPluginManager().registerEvents(new ChestShopListener(), this);
		}
		if (Hook.SHOPKEEPERS.isEnabled()) {
			getServer().getPluginManager().registerEvents(new ShopKeepersListener(), this);
		}
		if (Hook.SLIMEFUN.isEnabled() && getMCVersion().isOrLater(MinecraftVersion.V1_14)) {
			getServer().getPluginManager().registerEvents(new SlimefunListener(), this);
		}

		// Heartbeat
		BreweryPlugin.getScheduler().runTaskTimer(new BreweryRunnable(), 650, 1200);
		BreweryPlugin.getScheduler().runTaskTimer(new DrunkRunnable(), 120, 120);

		if (getMCVersion().isOrLater(MinecraftVersion.V1_9)) {
			BreweryPlugin.getScheduler().runTaskTimer(new CauldronParticles(), 1, 1);
		}


		// Register PlaceholderAPI Placeholders
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlaceholderAPI().register();
		}

		log("Using scheduler&7: &a" + scheduler.getClass().getSimpleName());
		log(this.getDescription().getName() + " enabled!");

		if (config.isUpdateCheck()) {
			new UpdateChecker(RESOURCE_ID).query(latestVersion -> {
				String currentVersion = getDescription().getVersion();

				if (UpdateChecker.parseVersion(latestVersion) > UpdateChecker.parseVersion(currentVersion)) {
					UpdateChecker.setUpdateAvailable(true);
					log(lang.getEntry("Etc_UpdateAvailable", "v" + currentVersion, "v" + latestVersion));
				}
				UpdateChecker.setLatestVersion(latestVersion);
			});
		}
	}

	@Override
	public void onDisable() {
		if (addonManager != null) addonManager.unloadAddons();

		// Disable listeners
		HandlerList.unregisterAll(this);

		// Stop schedulers
		BreweryPlugin.getScheduler().cancelTasks(this);

		if (breweryPlugin == null) {
			return;
		}

		// save Data to Disk
		if (dataManager != null) dataManager.exit(true, false);

		BSealer.unregisterRecipe();

		this.log(this.getDescription().getName() + " disabled!");
	}

	private void migrateBreweryDataFolder() {
		String pluginsFolder = getDataFolder().getParentFile().getPath();

		File breweryFolder = new File(pluginsFolder + File.separator + "Brewery");
		File breweryXFolder = new File(pluginsFolder + File.separator + "BreweryX");

		if (!breweryFolder.exists() || breweryXFolder.exists()) {
			return;
		}

		if (!breweryXFolder.exists()) {
			breweryXFolder.mkdirs();
		}

		File[] files = breweryFolder.listFiles();
		if (files != null) {
			for (File file : files) {
				try {
					Files.copy(file.toPath(), new File(breweryXFolder, file.getName()).toPath());
				} catch (IOException e) {
					errorLog("Failed to move file: " + file.getName(), e);
				}
			}
			log("&5Moved files from Brewery to BreweryX's data folder");
		}
	}


	/**
	 * For loading ingredients from ItemMeta.
	 * <p>Register a Static function that takes an ItemLoader, containing a DataInputStream.
	 * <p>Using the Stream it constructs a corresponding Ingredient for the chosen SaveID
	 *
	 * @param saveID The SaveID should be a small identifier like "AB"
	 * @param loadFct The Static Function that loads the Item, i.e.
	 *                public static AItem loadFrom(ItemLoader loader)
	 */
	public void registerForItemLoader(String saveID, Function<ItemLoader, Ingredient> loadFct) {
		ingredientLoaders.put(saveID, loadFct);
	}

	/**
	 * Unregister the ItemLoader
	 *
	 * @param saveID the chosen SaveID
	 */
	public void unRegisterItemLoader(String saveID) {
		ingredientLoaders.remove(saveID);
	}

	public static BreweryPlugin getInstance() {
		return breweryPlugin;
	}

	public static TaskScheduler getScheduler() {
		return scheduler;
	}

	public static MinecraftVersion getMCVersion() {
		return minecraftVersion;
	}

	public static AddonManager getAddonManager() {
		return addonManager;
	}

	public static void setDataManager(DataManager dataManager) {
		BreweryPlugin.dataManager = dataManager;
	}

	public static DataManager getDataManager() {
		return dataManager;
	}

	public static boolean isFolia() {
		return isFolia;
	}

	// Utility

	public void msg(CommandSender sender, String msg) {
		sender.sendMessage(color(ConfigManager.getConfig(Config.class).getPluginPrefix() + msg));
	}

	public void log(String msg) {
		Bukkit.getConsoleSender().sendMessage(color(ConfigManager.getConfig(Config.class).getPluginPrefix() + msg));
	}

	public void debugLog(String msg) {
		if (ConfigManager.getConfig(Config.class).isDebug()) {
			this.msg(Bukkit.getConsoleSender(), "&2[Debug] &f" + msg);
		}
	}

	public void warningLog(String msg) {
		Bukkit.getConsoleSender().sendMessage(color("&e[BreweryX] WARNING: " + msg));
	}

	public void errorLog(String msg) {
		String str = color("&c[BreweryX] ERROR: " + msg);
		Bukkit.getConsoleSender().sendMessage(str);
		if (ReloadCommand.getReloader() != null) { // I hate this, but I'm too lazy to go change all of it - Jsinco
			ReloadCommand.getReloader().sendMessage(str);
		}
	}

	public void errorLog(String msg, Throwable throwable) {
		errorLog(msg);
		errorLog("&6" + throwable.toString());
		for (StackTraceElement ste : throwable.getStackTrace()) {
			errorLog(ste.toString());
		}
	}

	public int parseInt(String string) {
		if (string == null) {
			return 0;
		}
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}

	public double parseDouble(String string) {
		if (string == null) {
			return 0;
		}
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}

	public float parseFloat(String string) {
		if (string == null) {
			return 0;
		}
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}


	public String color(String msg) {
		return BUtil.color(msg);
	}

	// Runnables

	public static class DrunkRunnable implements Runnable {
		@Override
		public void run() {
			if (!BPlayer.isEmpty()) {
				BPlayer.drunkenness();
			}
		}
	}

	public class BreweryRunnable implements Runnable {
		@Override
		public void run() {
			long start = System.currentTimeMillis();

            // runs every min to update cooking time

			for (BCauldron bCauldron : BCauldron.bcauldrons.values()) {
				BreweryPlugin.getScheduler().runTask(bCauldron.getBlock().getLocation(), () -> {
					if (!bCauldron.onUpdate()) {
						BCauldron.bcauldrons.remove(bCauldron.getBlock());
					}
				});
			}


			Barrel.onUpdate();// runs every min to check and update ageing time

			if (getMCVersion().isOrLater(MinecraftVersion.V1_14)) MCBarrel.onUpdate();
			if (Hook.BLOCKLOCKER.isEnabled()) BlocklockerBarrel.clearBarrelSign();

			BPlayer.onUpdate();// updates players drunkenness


			//DataSave.autoSave();
			dataManager.tryAutoSave();

			debugLog("BreweryRunnable: " + (System.currentTimeMillis() - start) + "ms");
		}

	}

	public static class CauldronParticles implements Runnable {


		@Override
		public void run() {
			Config config = ConfigManager.getConfig(Config.class);

			if (!config.isEnableCauldronParticles()) return;
			if (config.isMinimalParticles() && BCauldron.particleRandom.nextFloat() > 0.5f) {
				return;
			}
			BCauldron.processCookEffects();
		}
	}

}
