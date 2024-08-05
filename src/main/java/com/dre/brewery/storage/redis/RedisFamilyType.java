package com.dre.brewery.storage.redis;

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
    NORMAL_SHARD
}
