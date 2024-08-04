package com.dre.brewery.storage;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.MCBarrel;
import com.dre.brewery.Wakeup;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.integration.bstats.Stats;
import com.dre.brewery.storage.impls.FlatFileStorage;
import com.dre.brewery.storage.records.BreweryMiscData;
import com.dre.brewery.storage.records.ConfiguredDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class DataManager {

    protected static BreweryPlugin plugin = BreweryPlugin.getInstance();
    protected static long lastSave = System.currentTimeMillis();

    // todo: implement across brewery


    // todo: Legacy potions?

    public abstract Barrel getBarrel(UUID id);
    public abstract Collection<Barrel> getAllBarrels(boolean async);
    public abstract void saveAllBarrels(Collection<Barrel> barrels, boolean overwrite);
    public abstract void saveBarrel(Barrel barrel);
    public abstract void deleteBarrel(UUID id);


    public abstract BCauldron getCauldron(UUID id);
    public abstract Collection<BCauldron> getAllCauldrons(boolean async);
    public abstract void saveAllCauldrons(Collection<BCauldron> cauldrons, boolean overwrite);
    public abstract void saveCauldron(BCauldron cauldron);
    public abstract void deleteCauldron(UUID id);


    public abstract BPlayer getPlayer(UUID playerUUID);
    public abstract Collection<BPlayer> getAllPlayers(boolean async);
    public abstract void saveAllPlayers(Collection<BPlayer> players, boolean overwrite);
    public abstract void savePlayer(BPlayer player);
    public abstract void deletePlayer(UUID playerUUID);


    // TODO: Wakeups
    public abstract Wakeup getWakeup(UUID id);
    public abstract Collection<Wakeup> getAllWakeups(boolean async);
    public abstract void saveAllWakeups(Collection<Wakeup> wakeups, boolean overwrite);
    public abstract void saveWakeup(Wakeup wakeup);
    public abstract void deleteWakeup(UUID id);


    public abstract BreweryMiscData getBreweryMiscData();
    public abstract void saveBreweryMiscData(BreweryMiscData data);


    public void tryAutoSave() {
        long interval = BConfig.autoSaveInterval * 60000L;

        if (System.currentTimeMillis() - lastSave > interval) {
            saveAll(true);
            lastSave = System.currentTimeMillis();
            plugin.debugLog("Auto saved all data!");
        }
    }

    public void saveAll(boolean async) {
        Collection<Barrel> barrels = Barrel.getBarrels();
        Collection<BCauldron> cauldrons = BCauldron.getBcauldrons().values();
        Collection<BPlayer> bPlayers = BPlayer.getPlayers().values();
        Collection<Wakeup> wakeups = Wakeup.getWakeups();

        if (async) {
            BreweryPlugin.getScheduler().runTaskAsynchronously(() -> doSave(barrels, cauldrons, bPlayers, wakeups));
        } else {
            doSave(barrels, cauldrons, bPlayers, wakeups);
        }
    }

    private void doSave(Collection<Barrel> barrels, Collection<BCauldron> cauldrons, Collection<BPlayer> players, Collection<Wakeup> wakeups) {
        saveAllBarrels(barrels, true);
        saveAllCauldrons(cauldrons, true);
        saveAllPlayers(players, true);
        saveAllWakeups(wakeups, true);
    }

    protected void closeConnection() {
    }

    public void exit(boolean save, boolean async) {
        if (save) {
            saveAll(async);
        }
        // todo: save brewery misc data throughout plugin lifecycle or just at shutdown?
        this.saveBreweryMiscData(unloadMiscData());
        this.closeConnection(); // let databases close their connections
        plugin.debugLog("DataManager exited.");
    }

    public static DataManager createDataManager(ConfiguredDataManager record) {
        DataManager dataManager = switch (record.type()) {
            case FLATFILE -> new FlatFileStorage(record);
            case MYSQL -> throw new UnsupportedOperationException("Not implemented yet.");
        };

        loadMiscData(dataManager.getBreweryMiscData());
        plugin.debugLog("DataManager created: " + record.type());
        return dataManager;
    }



    // Utility

    private static void loadMiscData(BreweryMiscData miscData) {
        Brew.installTime = miscData.installTime();
        MCBarrel.mcBarrelTime = miscData.mcBarrelTime();
        Brew.loadPrevSeeds(miscData.prevSaveSeeds());


        Stats stats = plugin.stats;
        // Check the hash to prevent tampering with statistics - Note by original author
        if (miscData.brewsCreated().size() == 7 && miscData.brewsCreatedHash() == miscData.brewsCreated().hashCode()) {
            stats.brewsCreated = miscData.brewsCreated().get(0);
            stats.brewsCreatedCmd = miscData.brewsCreated().get(1);
            stats.exc = miscData.brewsCreated().get(2);
            stats.good = miscData.brewsCreated().get(3);
            stats.norm = miscData.brewsCreated().get(4);
            stats.bad = miscData.brewsCreated().get(5);
            stats.terr = miscData.brewsCreated().get(6);
        }
    }

    private static BreweryMiscData unloadMiscData() {
        List<Integer> brewsCreated = new ArrayList<>(7);
        Stats stats = plugin.stats;
        brewsCreated.addAll(List.of(stats.brewsCreated, stats.brewsCreatedCmd, stats.exc, stats.good, stats.norm, stats.bad, stats.terr));


        return new BreweryMiscData(
                Brew.installTime,
                MCBarrel.mcBarrelTime,
                Brew.getPrevSeeds(),
                brewsCreated,
                brewsCreated.hashCode()
        );
    }


    protected Location deserializeLocation(String locationString) {
        return deserializeLocation(locationString, false);
    }

    protected String serializeLocation(Location location) {
        return serializeLocation(location, false);
    }

    protected Location deserializeLocation(String locationString, boolean yawPitch) {
        if (locationString == null) {
            return null;
        }
        String[] loc = locationString.split(",");
        UUID worldUUID;
        try {
            worldUUID = UUID.fromString(loc[0]);
        } catch (IllegalArgumentException e) {
            plugin.errorLog("Invalid world UUID! " + loc[0], e);
            return null;
        }
        World world = Bukkit.getWorld(worldUUID);

        if (world == null) {
            plugin.errorLog("World not found! " + loc[0]); // TODO: add command to purge stuff in non-existent worlds
            return null;
        }

        if (yawPitch && loc.length == 6) {
            return new Location(world, plugin.parseInt(loc[1]), plugin.parseInt(loc[2]), plugin.parseInt(loc[3]), plugin.parseFloat(loc[4]), plugin.parseFloat(loc[5]));
        } else {
            return new Location(world, plugin.parseInt(loc[1]), plugin.parseInt(loc[2]), plugin.parseInt(loc[3]));
        }
    }

    protected String serializeLocation(Location location, boolean yawPitch) {
        if (location.getWorld() == null) {
            plugin.errorLog("Location must have a world! " + location);
            return null;
        }
        if (yawPitch) {
            return location.getWorld().getUID() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getYaw() + "," + location.getPitch();
        } else {
            return location.getWorld().getUID() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
        }
    }
}
