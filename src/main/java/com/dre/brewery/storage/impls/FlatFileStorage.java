package com.dre.brewery.storage.impls;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BIngredients;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.Wakeup;
import com.dre.brewery.configuration.sector.capsule.ConfiguredDataManager;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.storage.StorageInitException;
import com.dre.brewery.storage.records.BreweryMiscData;
import com.dre.brewery.storage.serialization.BukkitSerialization;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.BoundingBox;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class FlatFileStorage extends DataManager {

    private final File rawFile;
    private final YamlConfiguration dataFile;

    public FlatFileStorage(ConfiguredDataManager record) throws StorageInitException {
        String fileName = record.getDatabase() + ".yml";
        this.rawFile = new File(plugin.getDataFolder(), fileName);

        if (!rawFile.exists()) {
            try {
                rawFile.createNewFile();
            } catch (IOException e) {
                throw new StorageInitException("Failed to create file! " + fileName, e);
            }
        }

        this.dataFile = YamlConfiguration.loadConfiguration(rawFile);
    }


    private void save() {
        try {
            dataFile.save(rawFile);
        } catch (IOException e) {
            plugin.errorLog("Failed to save to Flatfile!", e);
        }
    }

    @Override
    public Barrel getBarrel(UUID id) {
        String path = "barrels." + id;

        Block spigot = deserializeLocation(dataFile.getString(path + ".spigot")).getBlock();
        BoundingBox bounds = BoundingBox.fromPoints(dataFile.getIntegerList(path + ".bounds"));
        float time = (float) dataFile.getDouble(path + ".time", 0.0);
        byte sign = (byte) dataFile.getInt(path + ".sign", 0);
        ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(dataFile.getString(path + ".items", null));


        return new Barrel(spigot, sign, bounds, items, time, id);
    }

    @Override
    public Collection<Barrel> getAllBarrels() {
        ConfigurationSection section = dataFile.getConfigurationSection("barrels");
        if (section == null) {
            return Collections.emptyList();
        }

        List<Barrel> barrels = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            Barrel barrel = getBarrel(BUtil.uuidFromString(key));
            if (barrel != null) {
                barrels.add(barrel);
            }
        }
        return barrels;
    }

    @Override
    public void saveAllBarrels(Collection<Barrel> barrels, boolean overwrite) {
        if (overwrite) {
            dataFile.set("barrels", null);
        }
        for (Barrel barrel : barrels) {
            saveBarrel(barrel);
        }
    }

    @Override
    public void saveBarrel(Barrel barrel) {
        String path = "barrels." + barrel.getId();

        dataFile.set(path + ".spigot", serializeLocation(barrel.getSpigot().getLocation()));
        dataFile.set(path + ".bounds", barrel.getBody().getBounds().serialize());
        dataFile.set(path + ".time", barrel.getTime());
        dataFile.set(path + ".sign", barrel.getBody().getSignoffset());
        dataFile.set(path + ".items", BukkitSerialization.itemStackArrayToBase64(barrel.getInventory().getContents()));
        save();
    }

    @Override
    public void deleteBarrel(UUID id) {
        dataFile.set("barrels." + id, null);
        save();
    }

    @Override
    public BCauldron getCauldron(UUID id) {
        String path = "cauldrons." + id;

        Block block = deserializeLocation(dataFile.getString(path + ".block")).getBlock();
        BIngredients ingredients = BIngredients.deserializeIngredients(dataFile.getString(path + ".ingredients"));
        int state = dataFile.getInt(path + ".state", 0);

        return new BCauldron(block, ingredients, state, id);
    }

    @Override
    public Collection<BCauldron> getAllCauldrons() {
        ConfigurationSection section = dataFile.getConfigurationSection("cauldrons");

        if (section == null) {
            return Collections.emptyList();
        }

        List<BCauldron> cauldrons = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            BCauldron cauldron = getCauldron(BUtil.uuidFromString(key));
            if (cauldron != null) {
                cauldrons.add(cauldron);
            }
        }
        return cauldrons;
    }

    @Override
    public void saveAllCauldrons(Collection<BCauldron> cauldrons, boolean overwrite) {
        if (overwrite) {
            dataFile.set("cauldrons", null);
        }
        for (BCauldron cauldron : cauldrons) {
            saveCauldron(cauldron);
        }
    }

    @Override
    public void saveCauldron(BCauldron cauldron) {
        String path = "cauldrons." + cauldron.getId();

        dataFile.set(path + ".block", serializeLocation(cauldron.getBlock().getLocation()));
        dataFile.set(path + ".ingredients", cauldron.getIngredients().serializeIngredients());
        dataFile.set(path + ".state", cauldron.getState());
        save();
    }


    @Override
    public void deleteCauldron(UUID id) {
        dataFile.set("cauldrons." + id, null);
        save();
    }



    @Override
    public BPlayer getPlayer(UUID playerUUID) {
        String path = "players." + playerUUID;

        int quality = dataFile.getInt(path + ".quality", 0);
        int drunkenness = dataFile.getInt(path + ".drunkenness", 0);
        int offlineDrunkenness = dataFile.getInt(path + ".offlineDrunkenness", 0);
        return new BPlayer(playerUUID, quality, drunkenness, offlineDrunkenness);
    }

    @Override
    public Collection<BPlayer> getAllPlayers() {
        ConfigurationSection section = dataFile.getConfigurationSection("players");

        if (section == null) {
            return Collections.emptyList();
        }

        List<BPlayer> players = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            BPlayer player = getPlayer(BUtil.uuidFromString(key));
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    @Override
    public void saveAllPlayers(Collection<BPlayer> players, boolean overwrite) {
        if (overwrite) {
            dataFile.set("players", null);
        }
        for (BPlayer player : players) {
            savePlayer(player);
        }
    }

    @Override
    public void savePlayer(BPlayer player) {
        String path = "players." + player.getUuid();

        dataFile.set(path + ".quality", player.getQuality());
        dataFile.set(path + ".drunkenness", player.getDrunkeness());
        dataFile.set(path + ".offlineDrunkenness", player.getOfflineDrunkeness());
        save();
    }

    @Override
    public void deletePlayer(UUID playerUUID) {
        dataFile.set("players." + playerUUID, null);
        save();
    }

    @Override
    public Wakeup getWakeup(UUID id) {
        String path = "wakeups." + id;
        Location wakeupLocation = deserializeLocation(dataFile.getString(path + ".location"), true);
        return new Wakeup(wakeupLocation, id);
    }

    @Override
    public Collection<Wakeup> getAllWakeups() {
        ConfigurationSection section = dataFile.getConfigurationSection("wakeups");

        if (section == null) {
            return Collections.emptyList();
        }

        List<Wakeup> wakeups = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            Wakeup wakeup = getWakeup(BUtil.uuidFromString(key));
            if (wakeup != null) {
                wakeups.add(wakeup);
            }
        }
        return wakeups;
    }

    @Override
    public void saveAllWakeups(Collection<Wakeup> wakeups, boolean overwrite) {
        if (overwrite) {
            dataFile.set("wakeups", null);
        }
        for (Wakeup wakeup : wakeups) {
            saveWakeup(wakeup);
        }
    }

    @Override
    public void saveWakeup(Wakeup wakeup) {
        String path = "wakeups." + wakeup.getId();
        dataFile.set(path + ".location", serializeLocation(wakeup.getLoc(), true));
        save();
    }

    @Override
    public void deleteWakeup(UUID id) {
        dataFile.set("wakeups." + id, null);
        save();
    }

    @Override
    public BreweryMiscData getBreweryMiscData() {
        return new BreweryMiscData(
                dataFile.getLong("misc.installTime", System.currentTimeMillis()),
                dataFile.getLong("misc.mcBarrelTime", 0),
                dataFile.getLongList("misc.previousSaveSeeds"),
                dataFile.getIntegerList("misc.brewsCreated"),
                dataFile.getInt("misc.brewsCreatedHash", 0)
        );
    }

    @Override
    public void saveBreweryMiscData(BreweryMiscData data) {
        dataFile.set("misc.installTime", data.installTime());
        dataFile.set("misc.mcBarrelTime", data.mcBarrelTime());
        dataFile.set("misc.previousSaveSeeds", data.prevSaveSeeds());
        dataFile.set("misc.brewsCreated", data.brewsCreated());
        dataFile.set("misc.brewsCreatedHash", data.brewsCreatedHash());
        save();
    }
}
