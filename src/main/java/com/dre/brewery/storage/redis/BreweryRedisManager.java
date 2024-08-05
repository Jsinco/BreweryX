package com.dre.brewery.storage.redis;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.Wakeup;
import com.dre.brewery.storage.RedisInitException;
import com.dre.brewery.storage.records.ConfiguredRedisManager;
import com.dre.brewery.storage.serialization.RedisSerializer;
import org.bukkit.block.Block;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BreweryRedisManager extends JedisPubSub {
    private static final BreweryPlugin plugin = BreweryPlugin.getInstance();
    // TODO: Use jedis pool, fix serialization of lists


    private final JedisPool redisPool;
    private final RedisSerializer serializer = new RedisSerializer();
    private final RedisFamilyType type;

    public BreweryRedisManager(ConfiguredRedisManager record) throws RedisInitException {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setBlockWhenExhausted(true);
        redisPool = new JedisPool(poolConfig, record.host(), record.port(), record.ssl());

        this.type = record.type();
        redis = new Jedis(record.host(), record.port());

        redis.auth(record.password());
        redis.connect();
        plugin.debugLog("Connected to Redis: " + record.host() + ":" + record.port());

        // Use a thread to subscribe to the Redis channel
        BreweryPlugin.getScheduler().runTaskAsynchronously(() -> {
            try (Jedis redisPubSub = new Jedis(record.host(), record.port())) {
                redisPubSub.auth(record.password());
                redisPubSub.connect();
                redisPubSub.subscribe(this, "brewery");
            } catch (Throwable e) {
                plugin.errorLog("Got a throwable while trying to initialize Redis manager", e);
                //throw new RedisInitException("Error initializing Redis manager", e);

            }
        });



        if (type == RedisFamilyType.MASTER_SHARD) {
            plugin.debugLog("I'm a master shard, starting cache update task.");
            BreweryPlugin.getScheduler().runTaskTimerAsynchronously(() -> {
                publish(RedisMessage.CACHE_UPDATE);
                plugin.debugLog("Sent cache update message to Redis");
            }, 0, 20);
        }
        try {

        } catch (Throwable e) {
            plugin.errorLog("Got a throwable while trying to initialize Redis manager", e);
            throw new RedisInitException("Error initializing Redis manager", e);
        }
    }

    public void publish(RedisMessage message) {
        String msg = type.name() + ";" + message.name();
        redis.publish("brewery", msg);
    }

    public Jedis getRedis() {
        return redis;
    }

    public Jedis getRedisPubSub() {
        return redisPubSub;
    }

    public RedisFamilyType getType() {
        return type;
    }

    // listeners

    @Override
    public void onMessage(String channel, String message) {
        plugin.debugLog("Received Redis channel: " + channel + " message: " + message);
        String[] parts = message.split(";");
        RedisFamilyType familyType = RedisFamilyType.valueOf(parts[0]);
        RedisMessage redisMessage = RedisMessage.valueOf(parts[1]);

       switch (redisMessage) {
           case CACHE_RETRIEVE -> retrieveCached(message);
           case CACHE_UPDATE -> pushCache(message);
           case SAVE -> save(message);
           case SHARD_ENABLED -> shardEnabled(message);
       }
    }



    @RedisSub(listenFor = RedisMessage.CACHE_RETRIEVE)
    public void retrieveCached(String rawMessage) {
        CopyOnWriteArrayList<Barrel> newList = serializer.deserialize(redis.get("barrels"), CopyOnWriteArrayList.class);
        ConcurrentHashMap<String, BPlayer> newPlayers = serializer.deserialize(redis.get("players"), ConcurrentHashMap.class);
        ConcurrentHashMap<Block, BCauldron> newCauldrons = serializer.deserialize(redis.get("cauldrons"), ConcurrentHashMap.class);
        CopyOnWriteArrayList<Wakeup> newWakeups = serializer.deserialize(redis.get("wakeups"), CopyOnWriteArrayList.class);


        for (Barrel barrel : newList) {
            Barrel.barrels.removeIf(b -> b.getId().equals(barrel.getId()));
            Barrel.barrels.add(barrel);
        }
        for (String uuid : newPlayers.keySet()) {
            BPlayer.getPlayers().put(uuid, newPlayers.get(uuid));
        }
        for (Block block : newCauldrons.keySet()) {
            BCauldron.bcauldrons.put(block, newCauldrons.get(block));
        }
        for (Wakeup wakeup : newWakeups) {
            Wakeup.wakeups.removeIf(w -> w.getId().equals(wakeup.getId()));
            Wakeup.wakeups.add(wakeup);
        }
        plugin.debugLog("Updated cache from Redis");
    }

    @RedisSub(listenFor = RedisMessage.CACHE_UPDATE)
    public void pushCache(String rawMessage) {
        retrieveCached(rawMessage); // Update the cache first

        redis.set("barrels", serializer.serialize(Barrel.barrels));
        redis.set("players", serializer.serialize(BPlayer.getPlayers()));
        redis.set("cauldrons", serializer.serialize(BCauldron.bcauldrons));
        redis.set("wakeups", serializer.serialize(Wakeup.wakeups));

        plugin.debugLog("Pushed cache to Redis");
    }

    @RedisSub(listenFor = RedisMessage.SAVE)
    public void save(String rawMessage) {
        if (type != RedisFamilyType.MASTER_SHARD) {
            plugin.debugLog("Received save message, but I'm not a master shard, ignoring.");
            return;
        }

        plugin.debugLog("Received save message, saving data to disk.");
        BreweryPlugin.getDataManager().saveAll(true);
    }


    @RedisSub(listenFor = RedisMessage.SHARD_ENABLED)
    public void shardEnabled(String rawMessage) {
        plugin.debugLog("A shard has been enabled: " + rawMessage);
    }
}
