package com.dre.brewery.hazelcast;

import com.dre.brewery.BreweryPlugin;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class BreweryHazelcast {

    private static boolean started = false;

    private HazelcastInstance hazelcastInstance;



    public BreweryHazelcast(String address, int port) {
        Config config = new Config();

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(port);
        networkConfig.setPortAutoIncrement(true); // Enable port auto-increment
        networkConfig.setPublicAddress(address);
        config.setClassLoader(BreweryPlugin.getInstance().getClass().getClassLoader());

        BreweryPlugin.getScheduler().runTaskAsynchronously(() -> {
            hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            started = true;
        });

        while (!started) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        hazelcastInstance.shutdown();
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }


    public enum HazelcastShard {
        MASTER, NODE
    }
}
