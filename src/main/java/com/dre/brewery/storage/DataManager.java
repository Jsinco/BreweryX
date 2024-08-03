package com.dre.brewery.storage;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.BreweryPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public interface DataManager {

    BreweryPlugin plugin = BreweryPlugin.getInstance();


    // todo: Legacy potions?

    Barrel getBarrel(UUID id, boolean async); // todo: remove this async bullshit added by original authors. context: go ctrl + b to source and find out what it actually does
    void saveBarrel(Barrel barrel);
    void deleteBarrel(UUID id);

    BCauldron getCauldron(UUID id);
    void saveCauldron(BCauldron cauldron);
    void deleteCauldron(UUID id);


    BPlayer getPlayer(UUID playerUUID);
    void savePlayer(BPlayer player);
    void deletePlayer(UUID playerUUID);


    // TODO: Wakeups


    default Location deserializeLocation(String locationString) {
        String[] loc = locationString.split(",");
        World world = Bukkit.getWorld(UUID.fromString(loc[0]));

        if (world == null) {
            plugin.errorLog("World not found! " + loc[0]); // TODO: add command to purge stuff in non-existent worlds
            return null;
        }

        return new Location(world, plugin.parseInt(loc[1]), plugin.parseInt(loc[2]), plugin.parseInt(loc[3]));
    }

    default String serializeLocation(Location location) {
        return location.getWorld().getUID() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
}
