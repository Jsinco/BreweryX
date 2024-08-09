package com.dre.brewery.hazelcast;

import com.dre.brewery.BreweryPlugin;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class BreweryHazelcast {

    private final Object lock = new Object();
    private HazelcastInstance hazelcastInstance;


    public BreweryHazelcast(String address, int port) {
        Config config = new Config();

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPublicAddress(address);
        networkConfig.setPort(port);
        networkConfig.setPortAutoIncrement(true); // Enable port auto-increment
        config.setClassLoader(BreweryPlugin.getInstance().getClass().getClassLoader());

        BreweryPlugin.getScheduler().runTaskAsynchronously(() -> {
            hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            synchronized (lock) {
                lock.notifyAll();
            }
        });

        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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
