package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.api.addons.AddonManager;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.configuration.files.Lang;
import com.dre.brewery.utility.Logging;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadAddonsCommand implements SubCommand {
	@Override
	public void execute(BreweryPlugin breweryPlugin, Lang lang, CommandSender sender, String label, String[] args) {
		if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
			AddonManager addonManager = new AddonManager(breweryPlugin);
			addonManager.unloadAddons();
			addonManager.loadAddons();
			Logging.msg(sender, "Loaded " + addonManager.getAddons().size() + " addons");
		} else {
			Logging.msg(sender, "&rThis command should be avoided as it can cause unpredictable behavior within addons, use &c/brewery reloadaddons confirm &r to confirm.");
			Logging.msg(sender, "&aMost addons support reloading without using this command! Try using &c/brewery reload &ainstead.");
		}
	}

	@Override
	public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
		return List.of("confirm");
	}

	@Override
	public String permission() {
		return "brewery.cmd.reloadaddons";
	}

	@Override
	public boolean playerOnly() {
		return false;
	}
}
