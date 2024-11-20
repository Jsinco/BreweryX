package com.dre.brewery.commands.subcommands;

import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.configuration.files.Lang;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DeleteCommand implements SubCommand {


    @Override
    public void execute(BreweryPlugin breweryPlugin, Lang lang, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (Brew.isBrew(hand)) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            return;
        }
        breweryPlugin.msg(sender, lang.getEntry("Error_ItemNotPotion"));

    }

    @Override
    public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        return null;
    }

    @Override
    public String permission() {
        return "brewery.cmd.delete";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }
}
