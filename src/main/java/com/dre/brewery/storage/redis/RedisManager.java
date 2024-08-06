package com.dre.brewery.storage.redis;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.Barrel;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.Wakeup;
import com.dre.brewery.storage.RedisInitException;
import com.dre.brewery.storage.records.ConfiguredRedisManager;
import com.dre.brewery.storage.records.SerializableBPlayer;
import com.dre.brewery.storage.records.SerializableBarrel;
import com.dre.brewery.storage.records.SerializableCauldron;
import com.dre.brewery.storage.records.SerializableWakeup;
import com.dre.brewery.storage.serialization.RedisSerializer;
import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.List;

// TODO: Use jedis pool, fix serialization of lists
public class RedisManager extends JedisPubSub {

    /*
    REWRITE ME/
     */

    private static final String BARRELS = "barrels", PLAYERS = "players", CAULDRONS = "cauldrons", WAKEUPS = "wakeups";
    private static final BreweryPlugin plugin = BreweryPlugin.getInstance();
    private static final JedisPoolConfig poolConfig = new JedisPoolConfig();
    static {
        poolConfig.setMaxTotal(12);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(60000);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
    }




    private final JedisPool redisPool;
    private final RedisSerializer serializer;
    private final RedisFamilyType type;
    private final String id;

    private MyScheduledTask masterCachingTask = null;


    public RedisManager(ConfiguredRedisManager record) throws RedisInitException {
        try {
            this.serializer =  new RedisSerializer();
            this.type = record.type();
            this.id = RedisFamilyType.generateUniqueId();

            redisPool = new JedisPool(poolConfig, record.host(), record.port(), 20000, record.password());
            plugin.log("Connected to Redis! &6" + record.host() + ":" + record.port());
            redisLog("Shard type: &6" + type.name() + " &7ID: &6" + id + "&7.");


            // Use a thread to subscribe to the Redis channel
            getSubscriber(this).runTaskAsynchronously(plugin);
            //publish(RedisMessage.SHARD_ENABLED);
        } catch (Throwable e) {
            throw new RedisInitException("Failed to connect to Redis", e);
        }
    }

    public UniversalRunnable getSubscriber(JedisPubSub pubSubClass) {
        return new UniversalRunnable() {
            @Override
            public void run() {
                try (Jedis redisPubSub = redisPool.getResource()) {
                    redisLog("Subscribing to Redis brewery channel, as&7: &6" + type.name());
                    redisPubSub.subscribe(pubSubClass, "brewery");
                } catch (Exception e) {
                    plugin.errorLog("Got an exception from a Redis publish subscriber", e);
                }
            }
        };
    }

    public JedisPool getRedisPool() {
        return redisPool;
    }

    public RedisFamilyType getType() {
        return type;
    }

    public void publish(RedisMessage message) {
        String msg = this.type.name() + ";" + message.name() + ";" + this.id;
        try (Jedis activeRedis = redisPool.getResource()) {
            activeRedis.publish("brewery", msg);
        }
    }

    public void startRedisCaching() {
        if (type != RedisFamilyType.MASTER_SHARD) {
            redisDebugLog("Not a master shard, not starting caching.");
            return; // Stuff below is for masters only
        }


        this.masterCachingTask = BreweryPlugin.getScheduler().runTaskTimerAsynchronously(() -> {
            try (Jedis redis = redisPool.getResource()) {
                redis.del(BARRELS);
                redis.del(PLAYERS);
                redis.del(CAULDRONS);
                redis.del(WAKEUPS);
            } catch (Exception e) {
                plugin.errorLog("Failed to clear cache from Redis", e);
            }
            //this.pushCache(RedisFamilyType.MASTER_SHARD, RedisMessage.CACHE_UPDATE, this.id); // Push our cache to redis
            //publish(RedisMessage.CACHE_UPDATE); // Tell all shards to push their cache to Redis
            this.retrieveCached(RedisFamilyType.MASTER_SHARD, RedisMessage.CACHE_RETRIEVE, this.id); // Retrieve all caches from Redis
            publish(RedisMessage.CACHE_RETRIEVE); // Tell all shards to retrieve their cache from Redis
            redisDebugLog("Sent cache update message to Redis");
        }, 0, 200);
    }

    public void exit() {
        if (masterCachingTask != null) {
            masterCachingTask.cancel();
        }

        redisPool.close();
        redisLog("Disconnected from Redis!");
    }


    // listeners

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split(";");
        String id = parts[2];

        if (id.equals(this.id)) {
            return;
        }

        RedisFamilyType familyType = RedisFamilyType.valueOf(parts[0]);
        RedisMessage redisMessage = RedisMessage.valueOf(parts[1]);


        redisDebugLog("Received Redis channel: " + channel + " message: " + message);
        switch (redisMessage) {
            case CACHE_RETRIEVE -> retrieveCached(familyType, redisMessage, id);
           // case CACHE_UPDATE -> pushCache(familyType, redisMessage, id);
            case SAVE -> save(familyType, redisMessage, id);
            //case SHARD_ENABLED -> shardEnabled(familyType, redisMessage, id);
        }
    }


    public void retrieveCached(RedisFamilyType fromType, RedisMessage message, String id) {

        try (Jedis redis = redisPool.getResource()) {
            List<Barrel> newBarrelsList = SerializableBarrel.toBarrels(serializer.deserializeArray(redis.lrange(BARRELS, 0, -1), SerializableBarrel.class));
            List<BPlayer> newPlayers = SerializableBPlayer.toBPlayers(serializer.deserializeArray(redis.lrange(PLAYERS, 0, -1), SerializableBPlayer.class));
            List<BCauldron> newCauldrons = SerializableCauldron.toCauldrons(serializer.deserializeArray(redis.lrange(CAULDRONS, 0, -1), SerializableCauldron.class));
            List<Wakeup> newWakeups = SerializableWakeup.toWakeups(serializer.deserializeArray(redis.lrange(WAKEUPS, 0, -1), SerializableWakeup.class));

            for (Barrel barrel : newBarrelsList) {
                Barrel.barrels.removeIf(b -> b.getId().equals(barrel.getId()));
                Barrel.barrels.add(barrel);
                System.out.println(barrel);
            }
            for (BPlayer player : newPlayers) {
                BPlayer.players.put(player.getUuid(), player);
                System.out.println(player);
            }
            for (BCauldron cauldron : newCauldrons) {
                BCauldron.bcauldrons.put(cauldron.getBlock(), cauldron);
                System.out.println(cauldron);
            }
            for (Wakeup wakeup : newWakeups) {
                Wakeup.wakeups.removeIf(w -> w.getId().equals(wakeup.getId()));
                Wakeup.wakeups.add(wakeup);
                System.out.println(wakeup);
            }
            redisDebugLog("Updated cache from Redis");
            redisDebugLog("&6Barrels: " + Barrel.barrels.size() + " Players: " + BPlayer.players.size() + " Cauldrons: " + BCauldron.bcauldrons.size() + " Wakeups: " + Wakeup.wakeups.size());
        } catch (Exception e) {
            plugin.errorLog("Failed to retrieve cache from Redis", e);
        }
    }


    public void pushCache(RedisFamilyType fromType, RedisMessage message, String id) {
        try (Jedis redis = redisPool.getResource()) {
            String[] barrelStrings = serializer.serializeArray(SerializableBarrel.fromBarrels(Barrel.barrels)),
                    playerStrings = serializer.serializeArray(SerializableBPlayer.fromBPlayers(BPlayer.players.values())),
                    cauldronStrings = serializer.serializeArray(SerializableCauldron.fromCauldrons(BCauldron.bcauldrons.values())),
                    wakeupStrings = serializer.serializeArray(SerializableWakeup.fromWakeups(Wakeup.wakeups));

            if (barrelStrings != null) redis.lpush(BARRELS, barrelStrings);
            if (playerStrings != null) redis.lpush(PLAYERS, playerStrings);
            if (cauldronStrings != null) redis.lpush(CAULDRONS, cauldronStrings);
            if (wakeupStrings != null) redis.lpush(WAKEUPS, wakeupStrings);
            redisDebugLog("Pushed cache to Redis");
        } catch (Exception e) {
            plugin.errorLog("Failed to push cache to Redis", e);
        }
    }


    public void save(RedisFamilyType fromType, RedisMessage message, String id) {
        if (type != RedisFamilyType.MASTER_SHARD) {
            redisDebugLog("Received save message, but I'm not a master shard, ignoring.");
            return;
        }

        redisLog("Received save message, saving data to disk.");
        BreweryPlugin.getDataManager().saveAll(true);
    }

    public void shardEnabled(RedisFamilyType fromType, RedisMessage message, String id) {
        redisLog("A shard has been enabled: " + fromType.name() + "id: " + id);
    }


    // Util - todo: move to another class?

    public static void redisLog(String message) {
        plugin.log("&4[Redis]&f " + message);
    }

    public static void redisDebugLog(String message) {
        plugin.debugLog("&4[Redis]&f " + message);
    }
}
