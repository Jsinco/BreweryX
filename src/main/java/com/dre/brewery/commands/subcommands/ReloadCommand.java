package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BCauldron;
import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.CommandUtil;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.storage.StorageInitException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ReloadCommand implements SubCommand {

    @Override
    public void execute(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
		if (sender != null && !sender.equals(Bukkit.getConsoleSender())) {
			BConfig.reloader = sender;
		}
		FileConfiguration cfg = BConfig.loadConfigFile();
		if (cfg == null) {
			// Could not read yml file, do not proceed, error was printed
			breweryPlugin.log("Something went wrong when trying to load the config file! Please check your config.yml");
			return;
		}

		// clear all existent config Data
		breweryPlugin. clearConfigData();

		// load the Config
		try {
			BConfig.readConfig(cfg);
		} catch (Exception e) {
			breweryPlugin.errorLog("Something went wrong when trying to load the config file! Please check your config.yml", e);
			return;
		}

		// Reload Cauldron Particle Recipes
		BCauldron.reload();

		// Clear Recipe completions
		CommandUtil.reloadTabCompleter();

		// Reload Recipes
		boolean successful = true;
		for (Brew brew : Brew.legacyPotions.values()) {
			if (!brew.reloadRecipe()) {
				successful = false;
			}
		}
		if (sender != null) {
			if (!successful) {
				breweryPlugin.msg(sender, breweryPlugin.languageReader.get("Error_Recipeload"));
			} else {
				breweryPlugin.msg(sender, breweryPlugin.languageReader.get("CMD_Reload"));
			}
		}

		DataManager dataManager = BreweryPlugin.getDataManager();
		if (dataManager != null){
			dataManager.exit(true, true, () -> {
				try {
					BreweryPlugin.setDataManager(DataManager.createDataManager(BConfig.configuredDataManager));
				} catch (StorageInitException e) {
					breweryPlugin.errorLog("Failed to initialize the DataManager! WARNING: This will cause issues and Brewery will NOT be able to save. Check your config and reload.", e);
				}
			});
		}

		BreweryPlugin.getAddonManager().reloadAddons();
		BConfig.reloader = null;
    }

    @Override
    public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        return null;
    }

    @Override
    public String permission() {
        return "brewery.cmd.reload";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }
}
