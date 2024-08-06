package com.dre.brewery.storage.redis;

public enum RedisMessage {

    // Sent by the MASTER shard to tell a NORMAL shard to push its cache
    PUSH_CACHE,
    // Sent by the MASTER shard to tell a NORMAL shard to retrieve its cache from Redis
    CACHE_RETRIEVE,
    // Sent by a NORMAL shard to tell the MASTER shard to save to disk
    SAVE,
    // Sent by a NORMAL shard to tell the MASTER shard that it has finished a task
    FINISHED_TASK,
    // Sent by a NORMAL shard to request a handshake with a MASTER shard
    HANDSHAKE_REQUEST,
    // Sent by a MASTER shard to respond to a handshake request
    HANDSHAKE_RESPONSE,
    // Sent by the MASTER shard to tell all shards that it's shutting down
    MASTER_SHUTDOWN,
    // Sent by the MASTER shard to tell all shards that it's online
    MASTER_ONLINE

}
