package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BPlayer;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.Wakeup;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.hazelcast.HazelcastCacheManager;
import com.dre.brewery.recipe.BRecipe;
import com.hazelcast.core.HazelcastInstance;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ShowStatsCommand implements SubCommand {
    @Override
    public void execute(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {

        breweryPlugin.getTaskScheduler().runTaskAsynchronously(() -> {
            HazelcastInstance hazelcast = BreweryPlugin.getHazelcast();

            breweryPlugin.msg(sender, "Drunk Players: " + BPlayer.numDrunkPlayers());
            breweryPlugin.msg(sender, "Brews created: " + BreweryPlugin.getInstance().stats.brewsCreated);
            breweryPlugin.msg(sender, "Barrels built: " + hazelcast.getList(HazelcastCacheManager.CacheType.BARRELS.getHazelcastName()).size());
            breweryPlugin.msg(sender, "Cauldrons boiling: " + hazelcast.getList(HazelcastCacheManager.CacheType.CAULDRONS.getHazelcastName()).size());
            breweryPlugin.msg(sender, "Number of Recipes: " + BRecipe.getAllRecipes().size());
            breweryPlugin.msg(sender, "Wakeups: " + hazelcast.getList(HazelcastCacheManager.CacheType.WAKEUPS.getHazelcastName()).size());
        });
    }

    @Override
    public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        return null;
    }

    @Override
    public String permission() {
        return "brewery.cmd.showstats";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }
}
