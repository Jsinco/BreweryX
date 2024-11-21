package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BSealer;
import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.CommandUtil;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.configurer.TranslationManager;
import com.dre.brewery.configuration.files.Lang;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand implements SubCommand {

	@Getter
	private static CommandSender reloader;

    @Override
    public void execute(BreweryPlugin breweryPlugin, Lang lang, CommandSender sender, String label, String[] args) {
		if (!sender.equals(Bukkit.getConsoleSender())) {
			reloader = sender;
		}


		try {
			// Reload translation manager
			TranslationManager.newInstance(breweryPlugin.getDataFolder());

			// Reload each config
			for (var entry : ConfigManager.LOADED_CONFIGS.entrySet()) {
				AbstractOkaeriConfigFile file = entry.getValue();
				try {
					file.reload();
				} catch (Throwable e) {
					breweryPlugin.errorLog("Something went wrong trying to load " + file.getBindFile().getFileName() + "!", e);
				}
			}

			// Reload Cauldron Ingredients
			ConfigManager.loadCauldronIngredients();
			// Reload Recipes
			ConfigManager.loadRecipes();

			// Reload Cauldron Particle Recipes
			BCauldron.reload();

			// Clear Recipe completions
			CommandUtil.reloadTabCompleter();

			// Sealing table recipe
			BSealer.registerRecipe();

			// Let addons know this command was executed
			BreweryPlugin.getAddonManager().reloadAddons();

			// Reload Recipes <-- TODO: Not really sure what this is doing...? - Jsinco
			boolean successful = true;
			for (Brew brew : Brew.legacyPotions.values()) {
				if (!brew.reloadRecipe()) {
					successful = false;
				}
			}

			if (!successful) {
				sender.sendMessage(lang.getEntry("Error_Recipeload"));
			} else {
				sender.sendMessage(lang.getEntry("CMD_Reload"));
			}


		} catch (Throwable e) {
			breweryPlugin.errorLog("Something went wrong trying to reload Brewery!", e);
		}
		// Make sure this reloader is set to null after
		reloader = null;
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
