package com.dre.brewery.hazelcast;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.Ownable;
import com.hazelcast.collection.IList;
import com.hazelcast.map.IMap;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class HazelcastUtil {

    private static final BreweryPlugin plugin = BreweryPlugin.getInstance();

    // Based on the number of instances, balance the objects between them
    // And gives ownership as equally possible
    public static <T extends Ownable> boolean splitArrayList(IList<T> list, HazelcastCacheManager.CacheType cacheType) {
        List<UUID> clusters = HazelcastCacheManager.getAllClusterIds();
        int clusterCount = clusters.size();

        if (clusterCount == 1) {
            return false;
        }

        int size = list.size();
        int partSize = size / clusterCount;
        int remainder = size % clusterCount;



        int startIndex = 0;

        for (int i = 0; i < clusterCount; i++) {
            // Calculate end index for the current cluster
            int endIndex = startIndex + partSize + (i < remainder ? 1 : 0);

            // Set owners for the sublist
            for (int j = startIndex; j < endIndex; j++) {
                list.get(j).setOwner(clusters.get(i));
            }

            // Update start index for the next cluster
            startIndex = endIndex;
        }


        /*switch (cacheType) {
            case BARRELS -> {
                for (T entry : list) {
                    ((Barrel) entry).saveToHazelcast();
                }
                plugin.debugLog("Barrels balanced: " + list.size());
            }

            case CAULDRONS -> {
                for (T entry : list) {
                    ((BCauldron) entry).saveToHazelcast();
                }
                plugin.debugLog("Cauldrons balanced: " + list.size());
            }

            default -> throw new IllegalArgumentException("Invalid cache type");
        }*/

        int i = 0;
        for (T entry : list) {
            list.set(i, entry);
        }
        plugin.debugLog("generic balance: " + list.size());
        return true; // or return a meaningful result based on your logic
    }



    public static <A, T extends Ownable> void splitMap(IMap<A, T> map, HazelcastCacheManager.CacheType cacheType) {
        List<UUID> clusters = HazelcastCacheManager.getAllClusterIds();
        int clusterCount = clusters.size();

        if (clusterCount == 1) {
            return; // No need to balance if there's only one cluster
        }

        // Extract the values from the map
        List<T> values = new ArrayList<>(map.values());
        int size = values.size();
        int partSize = size / clusterCount;
        int remainder = size % clusterCount;

        // Create a new map for the balanced data
        Map<A, T> balancedMap = new HashMap<>();

        // Track the current index in the values list
        int startIndex = 0;

        for (int i = 0; i < clusterCount; i++) {
            // Calculate end index for the current cluster
            int endIndex = startIndex + partSize + (i < remainder ? 1 : 0);

            // Set owners for the current partition
            for (int j = startIndex; j < endIndex; j++) {
                T value = values.get(j);
                value.setOwner(clusters.get(i));
                // You need to map the values back to their keys
                // Ensure you have a way to get the key associated with this value
                // For example, if you have a way to map back:
                A key = getKeyForValue(map, value);
                balancedMap.put(key, value);
            }

            // Update start index for the next cluster
            startIndex = endIndex;
        }

        // Now update Hazelcast cache
        /*switch (cacheType) {
            case PLAYERS -> {
                for (T entry : balancedMap.values()) {
                    ((BPlayer) entry).saveToHazelcast();
                }
                plugin.debugLog("BPlayers balanced: " + balancedMap.size());
            }

            default -> throw new IllegalArgumentException("Invalid cache type");
        }*/

        map.putAll(balancedMap);
        plugin.debugLog("generic balance: " + balancedMap.size());
    }

    private static <A, T extends Ownable> A getKeyForValue(Map<A, T> map, T value) {
        // Implement this method to find the key associated with a given value
        // This is a placeholder implementation; adjust as needed
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Key not found for value"));
    }
}
