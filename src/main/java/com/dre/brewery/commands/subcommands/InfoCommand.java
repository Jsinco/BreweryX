package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BPlayer;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.configuration.files.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InfoCommand implements SubCommand {


    @Override
    public void execute(BreweryPlugin breweryPlugin, Lang lang, CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            if (sender.hasPermission("brewery.cmd.infoOther")) {
                cmdInfo(sender, args[1], lang);
            } else {
                lang.sendEntry(sender, "Error_NoPermissions");
            }
        } else {
            if (sender.hasPermission("brewery.cmd.info")) {
                cmdInfo(sender, null, lang);
            } else {
                lang.sendEntry(sender, "Error_NoPermissions");
            }
        }
    }

    @Override
    public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        return null;
    }

    @Override
    public String permission() {
        return "brewery.cmd.info";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    public void cmdInfo(CommandSender sender, String playerName, Lang lang) {

        boolean selfInfo = playerName == null;
        if (selfInfo) {
            if (sender instanceof Player player) {
                playerName = player.getName();
            } else {
                lang.sendEntry(sender, "Error_PlayerCommand");
                return;
            }
        }

        Player player = BreweryPlugin.getInstance().getServer().getPlayerExact(playerName);
        BPlayer bPlayer;
        if (player == null) {
            bPlayer = BPlayer.getByName(playerName);
        } else {
            bPlayer = BPlayer.get(player);
        }
        if (bPlayer == null) {
            lang.sendEntry(sender, "CMD_Info_NotDrunk", playerName);
        } else {
            if (selfInfo) {
                bPlayer.showDrunkeness(player);
            } else {
                lang.sendEntry(sender, "CMD_Info_Drunk", playerName, "" + bPlayer.getDrunkeness(), "" + bPlayer.getQuality());
            }
        }

    }
}
