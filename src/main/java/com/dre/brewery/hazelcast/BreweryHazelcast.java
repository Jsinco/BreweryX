package com.dre.brewery.hazelcast;

import com.dre.brewery.BreweryPlugin;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class BreweryHazelcast {

    private final Object lock = new Object();
    private HazelcastInstance hazelcastInstance;


    public BreweryHazelcast(String address, int port, BreweryPlugin plugin) {
        Config config = new Config();

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPublicAddress(address);
        networkConfig.setPort(port);
        networkConfig.setPortAutoIncrement(true); // Enable port auto-increment
        config.setClassLoader(BreweryPlugin.getInstance().getClass().getClassLoader());
        config.setClusterName("breweryx");

        BreweryPlugin.getScheduler().runTaskAsynchronously(() -> {
            hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            synchronized (lock) {
                lock.notifyAll();
            }

            hazelcastInstance.getCluster().addMembershipListener(new HazelcastMemberShipListener());
            BreweryHazelcast.logMembers();
        });

        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            plugin.errorLog("Hazelcast initialization interrupted", e);
        }

        // Start balancing task - TEST
        BreweryPlugin.getScheduler().runTaskTimerAsynchronously(HazelcastCacheManager::balanceAll, 0, 3600L);
    }

    public void shutdown() {
        hazelcastInstance.shutdown();
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }


    public static void hazelcastLog(String message) {
        BreweryPlugin.getInstance().log("&d[Hazelcast] " + message);
    }

    public static void logMembers() {
        Cluster cluster = BreweryPlugin.getHazelcast().getCluster();
        BreweryHazelcast.hazelcastLog("Total member count&7:&d " + cluster.getMembers().size());
        for (Member member : cluster.getMembers()) {
            BreweryHazelcast.hazelcastLog("Member &e" + member.getAddress() + " &d- &6" + member.getUuid() + (member.localMember() ? " &a<-- this node" : ""));
        }
    }

    public enum HazelcastShard {
        MASTER, NODE
    }
}
