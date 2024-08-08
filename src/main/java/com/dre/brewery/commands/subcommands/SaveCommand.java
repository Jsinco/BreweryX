package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.storage.DataManager;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SaveCommand implements SubCommand {
    @Override
    public void execute(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        breweryPlugin.msg(sender, "Saving all Brewery data!");
        DataManager dataManager = BreweryPlugin.getDataManager();
        if (dataManager != null) {
            dataManager.saveAll(true);
        }
    }

    @Override
    public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        return null;
    }

    @Override
    public String permission() {
        return "brewery.cmd.save";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }
}
