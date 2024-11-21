package com.dre.brewery.commands.subcommands;

import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.configuration.files.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CopyCommand implements SubCommand {

    private final BreweryPlugin breweryPlugin;

    public CopyCommand(BreweryPlugin breweryPlugin) {
        this.breweryPlugin = breweryPlugin;
    }

    @Override
    public void execute(BreweryPlugin breweryPlugin, Lang lang, CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            cmdCopy(sender, breweryPlugin.parseInt(args[1]), lang);
        } else {
            cmdCopy(sender, 1, lang);
        }
    }

    @Override
    public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        return null;
    }

    @Override
    public String permission() {
        return "brewery.cmd.copy";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    //@Deprecated but still used?
    public void cmdCopy(CommandSender sender, int count, Lang lang) {
        if (count < 1 || count > 36) {
            sender.sendMessage(lang.getEntry("Etc_Usage"));
            sender.sendMessage(lang.getEntry("Help_Copy"));
            return;
        }
        Player player = (Player) sender;
        ItemStack hand = player.getItemInHand();
        if (hand != null) {
            if (Brew.isBrew(hand)) {
                while (count > 0) {
                    ItemStack item = hand.clone();
                    if (!(player.getInventory().addItem(item)).isEmpty()) {
                        sender.sendMessage(lang.getEntry("CMD_Copy_Error", "" + count));
                        return;
                    }
                    count--;
                }
                return;
            }
        }

        sender.sendMessage(lang.getEntry("Error_ItemNotPotion"));

    }
}
