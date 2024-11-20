package com.dre.brewery.storage;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.MCBarrel;
import com.dre.brewery.Wakeup;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.files.Config;
import com.dre.brewery.integration.bstats.Stats;
import com.dre.brewery.storage.impls.FlatFileStorage;
import com.dre.brewery.storage.impls.MySQLStorage;
import com.dre.brewery.storage.impls.SQLiteStorage;
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
        long interval = ConfigManager.getConfig(Config.class).getAutosave() * 60000L;

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
        Collection<Barrel> barrels = Barrel.getBarrels();
        Collection<BCauldron> cauldrons = BCauldron.getBcauldrons().values();
        Collection<BPlayer> bPlayers = BPlayer.getPlayers().values();
        Collection<Wakeup> wakeups = Wakeup.getWakeups();

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


    public void exit(boolean save, boolean async) {
        this.exit(save, async, null);
    }

    public void exit(boolean save, boolean async, Runnable callback) {
        if (save) {
            saveAll(async, () -> {
                this.closeConnection();
                plugin.log("Closed connection from&7:&a " + this.getClass().getSimpleName());
                if (callback != null) {
                    callback.run();
                }
            });
        } else {
            this.closeConnection(); // let databases close their connections
            plugin.log("Closed connection from&7:&a " + this.getClass().getSimpleName());
            if (callback != null) {
                callback.run();
            }
        }
    }

    public static DataManager createDataManager(ConfiguredDataManager record) throws StorageInitException {
        DataManager dataManager = switch (record.getType()) {
            case FLATFILE -> new FlatFileStorage(record);
            case MYSQL -> new MySQLStorage(record);
            case SQLITE -> new SQLiteStorage(record);
        };

        // Legacy data migration
        if (BData.checkForLegacyData()) {
            long start = System.currentTimeMillis();
            plugin.log("&5Brewery is loading data from a legacy format!");

            BData.readData();
            BData.finalizeLegacyDataMigration();

            dataManager.saveAll(false);

            plugin.log("&5Finished migrating legacy data! Took&7: &a" + (System.currentTimeMillis() - start) + "ms&5! Join our discord if you need assistance: &ahttps://discord.gg/3FkNaNDnta");
			plugin.warningLog("BreweryX can only load legacy data from worlds that exist. If you're trying to migrate old cauldrons, barrels, etc. And the worlds they're in don't exist, you'll need to migrate manually.");
        }


        plugin.log("DataManager created&7:&a " + record.getType().getFormattedName());
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


    public static Location deserializeLocation(String locationString) {
        return deserializeLocation(locationString, false);
    }

    public static String serializeLocation(Location location) {
        return serializeLocation(location, false);
    }

    public static Location deserializeLocation(String string, boolean yawPitch) {
        if (string == null) {
            plugin.warningLog("Location is null!");
            return null;
        }

        String locationString = string;
        String worldName = null;
        if (locationString.contains("?=")) {
            String[] split = locationString.split("\\?=");
            locationString = split[0];
            worldName = split[1];
        }

        String[] loc = locationString.split(",");
        UUID worldUUID = null;
        try {
            worldUUID = UUID.fromString(loc[0]);
        } catch (IllegalArgumentException ignored) {
        }


        World world = null;


        if (worldUUID != null) {
            world = Bukkit.getWorld(worldUUID);
        }

        if (world == null && worldName != null) {
            world = Bukkit.getWorld(worldName);
        }


        if (world == null) {
            plugin.warningLog("World not found! " + loc[0]); // TODO: add command to purge stuff in non-existent worlds
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
        String locationString;
        if (yawPitch) {
            locationString = location.getWorld().getUID() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getYaw() + "," + location.getPitch();
        } else {
            locationString = location.getWorld().getUID() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
        }

        // added this extra char separator so brewery can now parse locations via the world uuid or name
        locationString = locationString + "?=" + location.getWorld().getName();
        return locationString;
    }
}
