package com.dre.brewery.hazelcast;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.Ownable;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Brewery Objects are shared between instances thanks to Hazelcast.
 * Objects all have individual owners. Owners are assigned by default when they create the object.
 * They are also assigned when this manager balances out the object pools.
 * <p>
 * Any object can be interacted with or removed by any Brewery instance but only the objects owner will tick
 * the object.
 */
public class HazelcastCacheManager {

    private static final HazelcastInstance hazelcast = BreweryPlugin.getHazelcast();
    private static final BreweryPlugin plugin = BreweryPlugin.getInstance();



    public static List<Barrel> getOwnedBarrels() {
        IList<Barrel> barrels = hazelcast.getList(CacheType.BARRELS.getHazelcastName());
        List<Barrel> ownedBarrels = new ArrayList<>();
        UUID ownerID = getClusterId();

        for (Barrel barrel : barrels) {
            if (barrel.getOwner().equals(ownerID)) {
                ownedBarrels.add(barrel);
            }
        }
        System.out.println("Owned barrels: " + ownedBarrels.size());
        return ownedBarrels;
    }

    public static List<BCauldron> getOwnedCauldrons() {
        IList<BCauldron> cauldrons = hazelcast.getList(CacheType.CAULDRONS.getHazelcastName());
        List<BCauldron> ownedCauldrons = new ArrayList<>();
        UUID ownerID = getClusterId();

        for (BCauldron cauldron : cauldrons) {
            if (cauldron.getOwner().equals(ownerID)) {
                ownedCauldrons.add(cauldron);
            }
        }
        System.out.println("Owned cauldrons: " + ownedCauldrons.size());
        return ownedCauldrons;
    }

    public static Map<UUID, BPlayer> getOwnedPlayers() {
        IMap<UUID, BPlayer> players = hazelcast.getMap(CacheType.PLAYERS.getHazelcastName());

        Map<UUID, BPlayer> ownedPlayers = new HashMap<>();
        UUID ownerID = getClusterId();

        for (Map.Entry<UUID, BPlayer> entry : players.entrySet()) {
            if (entry.getValue().getOwner().equals(ownerID)) {
                ownedPlayers.put(entry.getKey(), entry.getValue());
            }
        }

        System.out.println("Owned players: " + ownedPlayers.size());
        return ownedPlayers;
    }


    public static <T> void init(List<T> list, CacheType cacheType) {
        init(list, cacheType, false);
    }

    public static <T, A> void init(Map<T, A> map, CacheType cacheType) {
        init(map, cacheType, false);
    }

    /**
     * Initialize the cache with the given list of objects.
     * @param list List of objects to cache from persistent data
     * @param cacheType Type of cache to initialize
     * @param force Whether to add to the list even if the list is not empty
     * @param <T> Type of object to cache
     */
    public static <T> void init(List<T> list, CacheType cacheType, boolean force) {
        switch (cacheType) {
            case BARRELS -> {
                IList<Barrel> barrels = hazelcast.getList(cacheType.getHazelcastName());

                if (barrels.isEmpty() || force) {
                    barrels.addAll((Collection<? extends Barrel>) list);
                } else {
                    plugin.log("List BARRELS is not empty. This must mean Brewery has already loaded up on another server and pulled from db. Skipping init.");
                    splitArrayList(barrels, cacheType);
                }
                System.out.println("Barrels cached: " + barrels.size());
            }

            case CAULDRONS -> {
                IList<BCauldron> cauldrons = hazelcast.getList(cacheType.getHazelcastName());

                if (cauldrons.isEmpty() || force) {
                    cauldrons.addAll((Collection<? extends BCauldron>) list);
                } else {
                    plugin.log("List CAULDRONS is not empty. This must mean Brewery has already loaded up on another server and pulled from db. Skipping init.");
                    splitArrayList(cauldrons, cacheType);
                }

                System.out.println("Cauldrons cached: " + cauldrons.size());
            }

            case WAKEUPS -> {
                IList<UUID> wakeups = hazelcast.getList(cacheType.getHazelcastName());
                if (wakeups.isEmpty() || force) {
                    wakeups.addAll((Collection<? extends UUID>) list);
                } else {
                    plugin.log("List WAKEUPS is not empty. This must mean Brewery has already loaded up on another server and pulled from db. Skipping init.");
                }
                System.out.println("Wakeups cached: " + wakeups.size());
            }

            default -> {
                throw new IllegalArgumentException("Invalid cache type");
            }
        }
    }


    public static <T, A> void init(Map<T, A> map, CacheType cacheType, boolean force) {
        switch (cacheType) {
            case PLAYERS -> {
                IMap<UUID, BPlayer> players = hazelcast.getMap(cacheType.getHazelcastName());

                if (players.isEmpty() || force) {
                    players.putAll((Map<? extends UUID, ? extends BPlayer>) map);
                } else {
                    plugin.log("Map PLAYERS is not empty. This must mean Brewery has already loaded up on another server and pulled from db. Skipping init.");
                    splitMap(players, cacheType);
                }
            }

            default -> {
                throw new IllegalArgumentException("Invalid cache type");
            }
        }
    }


    // Based on the number of instances, balance the objects between them
    // And gives ownership as equally possible
    public static <T extends Ownable> boolean splitArrayList(List<T> list, CacheType cacheType) {
        List<UUID> clusters = getAllClusterIds();
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

        switch (cacheType) {
            case BARRELS -> {
                IList<Barrel> barrels = hazelcast.getList(CacheType.BARRELS.getHazelcastName());

                for (int i = 0; i < list.size(); i++) {
                    barrels.set(i, (Barrel) list.get(i));
                }
                System.out.println("Barrels balanced: " + barrels.size());
            }

            case CAULDRONS -> {
                IList<BCauldron> cauldrons = hazelcast.getList(CacheType.CAULDRONS.getHazelcastName());
                for (int i = 0; i < list.size(); i++) {
                    cauldrons.set(i, (BCauldron) list.get(i));
                }
                System.out.println("Cauldrons balanced: " + cauldrons.size());
            }

            default -> {
                throw new IllegalArgumentException("Invalid cache type");
            }
        }
        return true; // or return a meaningful result based on your logic
    }



    public static <A, T extends Ownable> void splitMap(Map<A, T> map, CacheType cacheType) {
        List<UUID> clusters = getAllClusterIds();
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
        switch (cacheType) {
            case PLAYERS -> {
                IMap<UUID, BPlayer> players = hazelcast.getMap(CacheType.PLAYERS.getHazelcastName());
                for (Map.Entry<A, T> entry : balancedMap.entrySet()) {
                    players.set((UUID) entry.getKey(), (BPlayer) entry.getValue());
                }
                System.out.println("Players balanced: " + players.size());
            }

            default -> {
                throw new IllegalArgumentException("Invalid cache type");
            }
        }
    }


    // utility methods


    public static UUID getClusterId() {
        return hazelcast.getCluster().getLocalMember().getUuid();
    }

    // Get all cluster ids
    public static List<UUID> getAllClusterIds() {
        Cluster cluster = hazelcast.getCluster();
        List<UUID> clusterIds = new ArrayList<>();
        for (Member member : cluster.getMembers()) {
            clusterIds.add(member.getUuid());
        }
        return clusterIds;
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


    public enum CacheType {
        BARRELS("barrels"), PLAYERS("players"), CAULDRONS("cauldrons"), WAKEUPS("wakeups");

        private final String hazelcastName;

        CacheType(String hazelcastName) {
            this.hazelcastName = hazelcastName;
        }

        public String getHazelcastName() {
            return hazelcastName;
        }
    }
}
