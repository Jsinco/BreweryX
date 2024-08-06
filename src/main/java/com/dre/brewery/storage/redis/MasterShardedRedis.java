package com.dre.brewery.storage.redis;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.storage.RedisInitException;
import com.dre.brewery.storage.records.ConfiguredRedisManager;
import com.github.Anon8281.universalScheduler.UniversalRunnable;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Set;

public class MasterShardedRedis extends AbstractRedisPubSub {
    // tbh probably need to rewrite again :sob:

    private static final Set<String> shardIds = new HashSet<>();
    private final Object lock = new Object();
    UniversalRunnable thread;

    public MasterShardedRedis(ConfiguredRedisManager record) throws RedisInitException {
        super(record);

        thread = new UniversalRunnable() {
            @Override
            public void run() {

                for (String shardId : shardIds) { // Loop through every shard
                    publish(RedisMessage.PUSH_CACHE, shardId); // Tell the shard to push its cache

                    synchronized (lock) {
                        // Wait for shard to respond
                        try {
                            lock.wait(5000);
                        } catch (InterruptedException e) {
                            redisLog("&6" + shardId + " &7did not respond in 5s!");
                            break;
                        }
                    }

                    retrieveCache(); // Retrieve the cache from the shard and add it to the master cache
                }

                pushCache(); // Push the master cache to Redis

                for (String shardId : shardIds) {
                    publish(RedisMessage.CACHE_RETRIEVE, shardId); // Tell all shards to retrieve the cache
                }

            }
        };

        thread.runTaskTimerAsynchronously(plugin, 0, 100);
    }


    @Override
    public void handshakeRequest(String normalShardId) {
        if (shardIds.contains(normalShardId)) {
            redisLog("&cGot a handshake request from a shard that is already connected: &6" + normalShardId);
            return;
        }
        shardIds.add(normalShardId);
        publish(RedisMessage.HANDSHAKE_RESPONSE, normalShardId);
        redisLog("Got a handshake request from: &6" + normalShardId + " &7Total shards: &6" + shardIds.size());
    }

    @Override
    public void handshakeResponse(String masterId) {
        // This is a master shard
    }

    @Override
    public void pushCache() {
        genericPushCache();
    }

    @Override
    public void retrieveCache() {
        genericRetrieveCache();
    }

    @Override
    public void save() {
        redisLog("Got a save request from another shard!");
        BreweryPlugin.getDataManager().saveAll(true);
    }

    @Override
    public void finishedTask(String from) {
        if (!shardIds.contains(from)) {
            redisLog("Got a finished task response from an unknown shard: &6" + from);
        }
        synchronized (lock) {
            lock.notify();
        }
        redisLog("Got a finished task response from: &6" + from);
    }

    @Override
    public void masterShutdown() {
        // This is a master shard
    }

    @Override
    public void exit() {
        thread.cancel();
        publish(RedisMessage.MASTER_SHUTDOWN, null);
        super.exit();
    }
}
