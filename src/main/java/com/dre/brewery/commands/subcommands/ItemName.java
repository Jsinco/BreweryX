package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.configuration.files.Lang;
import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

public class ItemName implements SubCommand {
    @Override
    public void execute(BreweryPlugin breweryPlugin, Lang lang, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        @SuppressWarnings("deprecation")
        ItemStack hand = BreweryPlugin.getMCVersion().isOrLater(MinecraftVersion.V1_9) ? player.getInventory().getItemInMainHand() : player.getItemInHand();
        if (hand != null) {
            sender.sendMessage(lang.getEntry("CMD_Configname", hand.getType().name().toLowerCase(Locale.ENGLISH)));
        } else {
            sender.sendMessage(lang.getEntry("CMD_Configname_Error"));
        }
    }

    @Override
    public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        return null;
    }

    @Override
    public String permission() {
        return "brewery.cmd.itemname";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }
}
