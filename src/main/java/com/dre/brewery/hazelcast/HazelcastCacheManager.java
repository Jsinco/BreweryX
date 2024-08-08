package com.dre.brewery.hazelcast;

import com.dre.brewery.Barrel;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.Ownable;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        IList<Barrel> barrels = hazelcast.getList(CacheType.BARRELS.getHazelCastName());
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



    public static <T> void init(List<T> list, CacheType cacheType) {
        switch (cacheType) {
            case BARRELS -> {
                IList<Barrel> barrels = hazelcast.getList(cacheType.getHazelCastName());

                if (barrels.isEmpty()) {
                    barrels.addAll((Collection<? extends Barrel>) list);
                } else {
                    plugin.log("List BARRELS is not empty. This must mean Brewery has already loaded up on another server and pulled from db. Skipping init.");
                    balance(barrels, cacheType);
                }


                for (Barrel barrel : barrels) {
                    System.out.println(barrel);
                }
                System.out.println("Barrels cached: " + barrels.size());
            }
            default -> {
                throw new IllegalArgumentException("Invalid cache type");
            }
        }
    }





    // Based on the number of instances, balance the objects between them
    // And gives ownership as equally possible
    public static <T extends Ownable> void balance(List<T> list, CacheType cacheType) {
        List<UUID> clusters = getAllClusterIds();
        int clusterCount = clusters.size();

        if (clusterCount == 1) {
            return;
        }

        int totalObjects = list.size();
        int objectsPerCluster = totalObjects / clusterCount;
        int remainder = totalObjects % clusterCount;

        int objectsAssigned = 0;
        for (int i = 0; i < clusterCount; i++) {
            int objectsToAssign = objectsPerCluster;
            if (remainder > 0) {
                objectsToAssign++;
                remainder--;
            }

            for (int j = 0; j < objectsToAssign; j++) {
                T object = list.get(objectsAssigned);
                objectsAssigned++;
                // Assign object to cluster
                object.setOwner(clusters.get(i));
            }
        }

        // Update the cache
        IList<T> cache = hazelcast.getList(cacheType.getHazelCastName());
        cache.clear();
        cache.addAll(list);

        plugin.log("Balanced " + cacheType.getHazelCastName() + " cache between " + clusterCount + " clusters");
    }


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


    public enum CacheType {
        BARRELS("barrels"), PLAYERS("players"), CAULDRONS("cauldrons");

        private final String hazelCastName;

        CacheType(String hazelCastName) {
            this.hazelCastName = hazelCastName;
        }

        public String getHazelCastName() {
            return hazelCastName;
        }
    }
}
