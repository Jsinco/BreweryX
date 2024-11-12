package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.storage.StorageInitException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DataManagerCommand implements SubCommand {
	@Override
	public void execute(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			breweryPlugin.msg(sender, "Missing arguments.");
			return;
		}

		if (args[1].equalsIgnoreCase("reload")) {
			BreweryPlugin.getDataManager().exit(true, true, () -> {
				try {
					BreweryPlugin.setDataManager(DataManager.createDataManager(BConfig.configuredDataManager));
					breweryPlugin.msg(sender, "Reloaded the DataManager!");
				} catch (StorageInitException e) {
					breweryPlugin.errorLog("Failed to initialize the DataManager! WARNING: This will cause issues and Brewery will NOT be able to save. Check your config and reload.", e);
				}
			});
		} else if (args[1].equalsIgnoreCase("save")) {
			BreweryPlugin.getDataManager().saveAll(true, () -> breweryPlugin.msg(sender, "Saved all Brewery data!"));

		} else {
			breweryPlugin.msg(sender, "Unknown argument: " + args[1]);
		}
	}

	@Override
	public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
		return List.of("reload", "save");
	}

	@Override
	public String permission() {
		return "brewery.cmd.datamanager";
	}

	@Override
	public boolean playerOnly() {
		return false;
	}
}
