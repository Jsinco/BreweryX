package com.dre.brewery.storage;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.Wakeup;
import com.dre.brewery.storage.serialization.RedisSerializer;
import com.github.Anon8281.universalScheduler.UniversalRunnable;
import org.bukkit.block.Block;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class designed to cache Brewery's objects for faster access.
 * <p>
 * Barrel
 * BCauldron
 * BPlayer
 * Wakeup
 */
public class CachedObjects {
    static Jedis jedis;
    private final RedisSerializer serializer = new RedisSerializer();
    private static final BreweryPlugin plugin = BreweryPlugin.getInstance();


    public static volatile List<Barrel> barrels = new CopyOnWriteArrayList<>();
    public static volatile Map<Block, BCauldron> cauldrons = new ConcurrentHashMap<>(); // All active cauldrons. Mapped to their block for fast retrieve
    public static volatile ConcurrentHashMap<String, BPlayer> players = new ConcurrentHashMap<>();// Players uuid and BPlayer
    public static volatile List<Wakeup> wakeups = new CopyOnWriteArrayList<>();


    public UniversalRunnable getRedisPublisher() {
        return new UniversalRunnable() {
            @Override
            public void run() {
                jedis.set("barrels", serializer.serialize(barrels));
                jedis.set("cauldrons", serializer.serialize(cauldrons));
                jedis.set("players", serializer.serialize(players));
                jedis.set("wakeups", serializer.serialize(wakeups));

                jedis.publish("brewery", "update_cache");
                plugin.debugLog("Sent Redis channel: brewery message: update_cache");
            }
        };
    }

    public JedisPubSub getRedisSubscriber() {
        return new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                plugin.debugLog("Received Redis channel: " + channel + " message: " + message);


                barrels = serializer.deserialize(jedis.get("barrels"), CopyOnWriteArrayList.class);
                cauldrons = serializer.deserialize(jedis.get("cauldrons"), ConcurrentHashMap.class);
                players = serializer.deserialize(jedis.get("players"), ConcurrentHashMap.class);
                wakeups = serializer.deserialize(jedis.get("wakeups"), CopyOnWriteArrayList.class);
                plugin.debugLog("Cache updated");
            }
        };
    }

}
