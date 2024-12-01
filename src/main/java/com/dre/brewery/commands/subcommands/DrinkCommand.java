package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BPlayer;
import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.CommandUtil;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.configuration.files.Lang;
import com.dre.brewery.utility.Tuple;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DrinkCommand implements SubCommand {
    @Override
    public void execute(BreweryPlugin breweryPlugin, Lang lang, CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            lang.sendEntry(sender, "Etc_Usage");
            lang.sendEntry(sender, "Help_Drink");
            return;
        }

        Tuple<Brew, Player> brewForPlayer = CommandUtil.getFromCommand(sender, args);
        if (brewForPlayer != null) {
            Player player = brewForPlayer.b();
            Brew brew = brewForPlayer.a();
            String brewName = brew.getCurrentRecipe().getName(brew.getQuality());
            BPlayer.drink(brew, null, player);

            lang.sendEntry(sender, "CMD_Drink", brewName);
            if (!sender.equals(player)) {
                lang.sendEntry(sender, "CMD_DrinkOther", player.getDisplayName(), brewName);
            }
        }
    }

    @Override
    public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        return CommandUtil.recipeNamesAndIds(args);
    }

    @Override
    public String permission() {
        return "brewery.cmd.drink";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }
}
