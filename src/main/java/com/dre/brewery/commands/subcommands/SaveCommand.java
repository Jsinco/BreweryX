package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.storage.redis.RedisManager;
import com.dre.brewery.storage.redis.RedisMessage;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SaveCommand implements SubCommand {
    @Override
    public void execute(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        breweryPlugin.msg(sender, "Saving all Brewery data!");
        DataManager dataManager = BreweryPlugin.getDataManager();
        RedisManager redisManager = BreweryPlugin.getRedisManager();
        if (dataManager != null) {
            dataManager.saveAll(true);
        }
        if (redisManager != null) {
            redisManager.publish(RedisMessage.SAVE);
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
