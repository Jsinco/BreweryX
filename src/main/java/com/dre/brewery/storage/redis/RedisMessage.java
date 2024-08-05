package com.dre.brewery.storage.redis;

public enum RedisMessage {
    /**
     * Sending a message to all shards to push their cache to Redis. This operation will not overwrite any data in Redis.
     * <p>
     * This message can be sent by any shard.
     */
    CACHE_UPDATE,
    /**
     * Sending a message to all shards to overwrite their cache with the data from Redis.
     * <p>
     * This message should only be sent by the master shard.
     */
    CACHE_RETRIEVE,
    /**
     * Sending a message that this shard has come online.
     * This shard will fill its cache with data from Redis.
     * <p>
     * This message can be sent by any shard.
     */
    SHARD_ENABLED,
    /**
     * Sending a message that this shard is going offline.
     * <p>
     * This message can be sent by any shard.
     */
    SHARD_DISABLED,
    /**
     * Sending a message to Redis telling the master shard to save its data to disk.
     * <p>
     * This message can be sent by any shard. But only the master shard will take action.
     */
    SAVE,

}
