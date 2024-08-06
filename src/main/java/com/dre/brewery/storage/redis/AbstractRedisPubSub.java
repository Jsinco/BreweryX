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
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractRedisPubSub extends JedisPubSub {


    private static final JedisPoolConfig POOL_CONFIG = new JedisPoolConfig();

    protected static final String CHANNEL = "breweryx", BARRELS = "barrels", PLAYERS = "players", CAULDRONS = "cauldrons", WAKEUPS = "wakeups";
    protected static final BreweryPlugin plugin = BreweryPlugin.getInstance();

    protected final RedisSerializer serializer = new RedisSerializer();
    protected final JedisPool redisPool;
    protected final RedisFamilyType type;
    protected final String id;

    public AbstractRedisPubSub(ConfiguredRedisManager record) throws RedisInitException {
        this(record.type(), record.getIdSafely(), record.host(), record.port(), record.password());
    }

    public AbstractRedisPubSub(RedisFamilyType type, String id, String host, int port, String password) throws RedisInitException {
        this.type = type;
        this.id = id;

        try {
            redisPool = new JedisPool(POOL_CONFIG, host, port, 20000, password);
            redisLog("Connected to Redis! &6" + host + ":" + port);

            plugin.getTaskScheduler().runTaskAsynchronously(() -> {
                try (Jedis redisPubSub = redisPool.getResource()) {
                    redisLog("Using thread: &6" + Thread.currentThread().getName() + " &aas Redis PubSub for: &6" + type + " &7ID: &6" + id);
                    redisPubSub.subscribe(this, CHANNEL);
                } catch (Exception e) {
                    plugin.errorLog("Got an exception from a Redis PubSub ID: &6" + id + "TYPE: " + type, e);
                    Thread.currentThread().interrupt();
                }
            });
        } catch (Exception e) {
            throw new RedisInitException("Failed to connect to Redis! " + host + ":" + port, e);
        }
    }

    // todo: maybe rewrite
    public abstract void handshakeRequest(String normalShardId);
    public abstract void handshakeResponse(String masterId);
    public abstract void pushCache();
    public abstract void retrieveCache();
    public abstract void save();
    public abstract void finishedTask(String from);
    public abstract void masterShutdown();


    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split(";");

        RedisFamilyType type = RedisFamilyType.valueOf(parts[0]);
        RedisMessage msg = RedisMessage.valueOf(parts[1]);
        String talkingTo = parts[2];
        String from = parts[3];

        if (from.equals(this.id)) {
            return;
        } else if (!talkingTo.equals(this.id) && !talkingTo.equals("null")) {
            return;
        }


        switch (msg) {
            case HANDSHAKE_REQUEST -> handshakeRequest(from);
            case HANDSHAKE_RESPONSE -> handshakeResponse(from);
            case PUSH_CACHE -> pushCache();
            case CACHE_RETRIEVE -> retrieveCache();
            case SAVE -> save();
            case FINISHED_TASK -> finishedTask(from);
            case MASTER_SHUTDOWN -> masterShutdown();
        }
    }
    // end rewrite

    public void publish(RedisMessage message, String who) {
        String msg = this.type + ";" + message + ";" + who + ";" + this.id;
        try (Jedis activeRedis = redisPool.getResource()) {
            activeRedis.publish(CHANNEL, msg);
        }
    }

    public void exit() {
        redisPool.close();
        redisLog("Disconnected from Redis!");
    }


    protected void genericPushCache() {
        try (Jedis redis = redisPool.getResource()) {
            String barrelStrings = serializer.serialize(SerializableBarrel.fromBarrels(Barrel.barrels)),
                    playerStrings = serializer.serialize(SerializableBPlayer.fromBPlayers(BPlayer.players.values())),
                    cauldronStrings = serializer.serialize(SerializableCauldron.fromCauldrons(BCauldron.bcauldrons.values())),
                    wakeupStrings = serializer.serialize(SerializableWakeup.fromWakeups(Wakeup.wakeups));

            if (barrelStrings != null) redis.set(BARRELS, barrelStrings);
            if (playerStrings != null) redis.set(PLAYERS, playerStrings);
            if (cauldronStrings != null) redis.set(CAULDRONS, cauldronStrings);
            if (wakeupStrings != null) redis.set(WAKEUPS, wakeupStrings);
            redisLog("Pushed cache to Redis!");
            plugin.debugLog("SerializedBarrels: " + barrelStrings + " SerializedPlayers: " + playerStrings + " SerializedCauldrons: " + cauldronStrings + " SerializedWakeups: " + wakeupStrings);
        } catch (Exception e) {
            plugin.errorLog("Failed to push cache to Redis &6ID: " + id + " TYPE: " + type , e);
        }
    }

    protected void genericRetrieveCache() {
        try (Jedis redis = redisPool.getResource()) {
            List<SerializableBarrel> serializableBarrels = serializer.deserialize(redis.get(BARRELS), new TypeToken<List<SerializableBarrel>>() {}.getType());
            List<SerializableBPlayer> serializablePlayers = serializer.deserialize(redis.get(PLAYERS), new TypeToken<List<SerializableBPlayer>>() {}.getType());
            List<SerializableCauldron> serializableCauldrons = serializer.deserialize(redis.get(CAULDRONS), new TypeToken<List<SerializableCauldron>>() {}.getType());
            List<SerializableWakeup> serializableWakeups = serializer.deserialize(redis.get(WAKEUPS), new TypeToken<List<SerializableWakeup>>() {}.getType());
            //System.out.println(serializablePlayers);

            Barrel.barrels = SerializableBarrel.toBarrels(serializableBarrels).stream().filter(Objects::nonNull).toList();
            BPlayer.players = new ConcurrentHashMap<>(SerializableBPlayer.toBPlayers(serializablePlayers).stream().filter(Objects::nonNull).collect(Collectors.toMap(BPlayer::getUuid, Function.identity())));
            BCauldron.bcauldrons = SerializableCauldron.toCauldrons(serializableCauldrons).stream().filter(Objects::nonNull).collect(Collectors.toMap(BCauldron::getBlock, Function.identity()));
            Wakeup.wakeups = SerializableWakeup.toWakeups(serializableWakeups).stream().filter(Objects::nonNull).toList();

            redisLog("Retrieved cache from Redis!");
        } catch (Exception e) {
            plugin.errorLog("Failed to retrieve cache from Redis &6ID: " + id + " TYPE: " + type , e);
        }
    }

    public static void redisLog(String message) {
        plugin.log("&c[Redis]&a " + message);
    }

    static {
        POOL_CONFIG.setMaxTotal(12);
        POOL_CONFIG.setMaxIdle(128);
        POOL_CONFIG.setMinIdle(16);
        POOL_CONFIG.setTestOnBorrow(true);
        POOL_CONFIG.setTestOnReturn(true);
        POOL_CONFIG.setTestWhileIdle(true);
        POOL_CONFIG.setMinEvictableIdleTimeMillis(60000);
        POOL_CONFIG.setTimeBetweenEvictionRunsMillis(30000);
        POOL_CONFIG.setNumTestsPerEvictionRun(3);
        POOL_CONFIG.setBlockWhenExhausted(true);
    }
}
