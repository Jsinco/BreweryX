package com.dre.brewery.storage.redis;

import java.util.Random;

public enum RedisFamilyType {
    /**
     * When running Brewery off of Redis, there should be only ONE master shard.
     * Master shards are responsible for listening to all other shards.
     * The master shard is the only one which will store persistent data.
     */
    MASTER_SHARD,
    /**
     * When running Brewery off of Redis, there should be multiple normal shards.
     * Normal shards are responsible for listening to the master shard.
     * Normal shards will not store any persistent data. And will constantly send their caches to Redis to be cached by all other shards.
     */
    NORMAL_SHARD;

    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 5;
    private static final Random RANDOM = new Random();

    public static String generateUniqueId() {
        StringBuilder uniqueId = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int index = RANDOM.nextInt(CHAR_SET.length());
            uniqueId.append(CHAR_SET.charAt(index));
        }
        return uniqueId.toString();
    }
}
