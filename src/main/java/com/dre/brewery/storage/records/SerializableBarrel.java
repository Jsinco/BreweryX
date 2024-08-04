package com.dre.brewery.storage.records;

import java.util.List;
import java.util.UUID;

/**
 * Represents a barrel that can be serialized.
 * @param id The UUID of the barrel
 * @param serializedLocation The Block/Location of the Spigot of the barrel
 * @param bounds The bounds of the barrel
 * @param time no idea
 * @param sign The sign byte offset the barrel
 * @param serializedItems Serialized ItemStacks 'BukkitSerialization.itemStackArrayToBase64(ItemStack[])'
 */
public record SerializableBarrel(UUID id, String serializedLocation, List<Integer> bounds, float time, byte sign, String serializedItems) {
}
