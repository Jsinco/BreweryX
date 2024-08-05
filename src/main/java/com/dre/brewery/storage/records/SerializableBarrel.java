package com.dre.brewery.storage.records;

import com.dre.brewery.Barrel;
import com.dre.brewery.storage.DataManager;
import com.dre.brewery.storage.serialization.BukkitSerialization;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.BoundingBox;

import java.util.List;

/**
 * Represents a barrel that can be serialized.
 * @param id The UUID of the barrel
 * @param serializedLocation The Block/Location of the Spigot of the barrel
 * @param bounds The bounds of the barrel
 * @param time no idea
 * @param sign The sign byte offset the barrel
 * @param serializedItems Serialized ItemStacks 'BukkitSerialization.itemStackArrayToBase64(ItemStack[])'
 */
public record SerializableBarrel(String id, String serializedLocation, List<Integer> bounds, float time, byte sign, String serializedItems) implements SerializableThing {
    public SerializableBarrel(Barrel barrel) {
        this(barrel.getId().toString(), DataManager.serializeLocation(barrel.getSpigot().getLocation()), barrel.getBody().getBounds().serializeToIntList(), barrel.getTime(), barrel.getBody().getSignoffset(), BukkitSerialization.itemStackArrayToBase64(barrel.getInventory().getContents()));
    }

    public Barrel toBarrel() {
        return new Barrel(DataManager.deserializeLocation(serializedLocation).getBlock(), sign, BoundingBox.fromPoints(bounds), BukkitSerialization.itemStackArrayFromBase64(serializedItems), time, BUtil.uuidFromString(id));
    }

    @Override
    public String getId() {
        return id;
    }

    public static List<SerializableBarrel> fromBarrels(List<Barrel> barrels) {
        if (barrels == null) {
            return List.of();
        }
        return barrels.stream().map(SerializableBarrel::new).toList();
    }

    public static List<Barrel> toBarrels(List<SerializableBarrel> barrels) {
        if (barrels == null) {
            return List.of();
        }
        return barrels.stream().map(SerializableBarrel::toBarrel).toList();
    }

    @Override
    public String toString() {
        return "SerializableBarrel{" +
                "id='" + id + '\'' +
                ", serializedLocation='" + serializedLocation + '\'' +
                ", bounds=" + bounds +
                ", time=" + time +
                ", sign=" + sign +
                ", serializedItems='" + serializedItems + '\'' +
                '}';
    }
}
