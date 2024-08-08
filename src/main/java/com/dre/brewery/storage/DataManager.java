package com.dre.brewery.storage;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.MCBarrel;
import com.dre.brewery.Wakeup;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.hazelcast.HazelcastCacheManager;
import com.dre.brewery.integration.bstats.Stats;
import com.dre.brewery.storage.impls.FlatFileStorage;
import com.dre.brewery.storage.impls.MySQLStorage;
import com.dre.brewery.storage.records.BreweryMiscData;
import com.dre.brewery.storage.records.ConfiguredDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class DataManager {

    // TODO: Instead of using UUIDs for Barrels, Cauldrons, and Wakeups. We should figure out some hashing algorithm to generate a unique ID for each of them.

    protected static BreweryPlugin plugin = BreweryPlugin.getInstance();
    protected static long lastAutoSave = System.currentTimeMillis();

    public abstract Barrel getBarrel(UUID id);
    public abstract Collection<Barrel> getAllBarrels();
    public abstract void saveAllBarrels(Collection<Barrel> barrels, boolean overwrite);
    public abstract void saveBarrel(Barrel barrel);
    public abstract void deleteBarrel(UUID id);


    public abstract BCauldron getCauldron(UUID id);
    public abstract Collection<BCauldron> getAllCauldrons();
    public abstract void saveAllCauldrons(Collection<BCauldron> cauldrons, boolean overwrite);
    public abstract void saveCauldron(BCauldron cauldron);
    public abstract void deleteCauldron(UUID id);


    public abstract BPlayer getPlayer(UUID playerUUID);
    public abstract Collection<BPlayer> getAllPlayers();
    public abstract void saveAllPlayers(Collection<BPlayer> players, boolean overwrite);
    public abstract void savePlayer(BPlayer player);
    public abstract void deletePlayer(UUID playerUUID);


    public abstract Wakeup getWakeup(UUID id);
    public abstract Collection<Wakeup> getAllWakeups();
    public abstract void saveAllWakeups(Collection<Wakeup> wakeups, boolean overwrite);
    public abstract void saveWakeup(Wakeup wakeup);
    public abstract void deleteWakeup(UUID id);


    public abstract BreweryMiscData getBreweryMiscData();
    public abstract void saveBreweryMiscData(BreweryMiscData data);


    public void tryAutoSave() {
        long interval = BConfig.autoSaveInterval * 60000L;

        if (System.currentTimeMillis() - lastAutoSave > interval) {
            saveAll(true);
            lastAutoSave = System.currentTimeMillis();
            plugin.debugLog("Auto saved all data!");
        }
    }

    public void saveAll(boolean async) {
        saveAll(async, null);
    }

    public void saveAll(boolean async, Runnable callback) {
        Collection<Barrel> barrels = HazelcastCacheManager.getOwnedBarrels(); // FIXME: Cache.getOwnedBarrels();
        Collection<BCauldron> cauldrons = HazelcastCacheManager.getOwnedCauldrons(); // FIXME: BCauldron.getCauldrons().values();
        Collection<BPlayer> bPlayers = HazelcastCacheManager.getOwnedPlayers().values(); // FIXME: BPlayer.getPlayers().values();
        Collection<Wakeup> wakeups = BreweryPlugin.getHazelcast().getList(HazelcastCacheManager.CacheType.WAKEUPS.getHazelcastName());

        if (async) {
            BreweryPlugin.getScheduler().runTaskAsynchronously(() -> {
                doSave(barrels, cauldrons, bPlayers, wakeups);
                if (callback != null) {
                    callback.run();
                }
            });
        } else {
            doSave(barrels, cauldrons, bPlayers, wakeups);
            if (callback != null) {
                callback.run();
            }
        }
    }

    private void doSave(Collection<Barrel> barrels, Collection<BCauldron> cauldrons, Collection<BPlayer> players, Collection<Wakeup> wakeups) {
        saveBreweryMiscData(getLoadedMiscData());
        saveAllBarrels(barrels, true);
        saveAllCauldrons(cauldrons, true);
        saveAllPlayers(players, true);
        saveAllWakeups(wakeups, true);
        plugin.debugLog("Saved all data!");
    }

    protected void closeConnection() {
        // Implemented in subclasses that use database connections
    }

    public void exit(boolean save, boolean async, @Nullable Runnable callback) {
        if (save) {
            saveAll(async, () -> {
                this.closeConnection();
                plugin.log("DataManager exited.");
                if (callback != null) {
                    callback.run();
                }
            });
        } else {
            this.closeConnection(); // let databases close their connections
            plugin.log("DataManager exited.");
            if (callback != null) {
                callback.run();
            }
        }
    }

    public static DataManager createDataManager(ConfiguredDataManager record) throws StorageInitException {
        DataManager dataManager = switch (record.type()) {
            case FLATFILE -> new FlatFileStorage(record);
            case MYSQL -> new MySQLStorage(record);
        };

        // Legacy data migration
        if (BData.checkForLegacyData()) {
            long start = System.currentTimeMillis();
            plugin.log("&5Brewery is loading data from a legacy format!");

            BData.readData();
            BData.finalizeLegacyDataMigration();

            dataManager.saveAll(false);

            plugin.log("&5Finished migrating legacy data! Took&7: &5" + (System.currentTimeMillis() - start) + "ms");
        }

        plugin.log("DataManager created: " + record.type());
        return dataManager;
    }



    // Utility

    public static void loadMiscData(BreweryMiscData miscData) {
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

    public static BreweryMiscData getLoadedMiscData() {
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

    public static String serializeBlock(Block block) {
        return serializeLocation(block.getLocation());
    }

    public static Block deserializeBlock(String blockString) {
        return deserializeLocation(blockString).getBlock();
    }


    public static Location deserializeLocation(String locationString) {
        return deserializeLocation(locationString, false);
    }

    public static String serializeLocation(Location location) {
        return serializeLocation(location, false);
    }

    public static Location deserializeLocation(String locationString, boolean yawPitch) {
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

    public static String serializeLocation(Location location, boolean yawPitch) {
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
