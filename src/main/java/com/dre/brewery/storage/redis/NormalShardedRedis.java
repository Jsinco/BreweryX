package com.dre.brewery.storage.redis;

import com.dre.brewery.storage.RedisInitException;
import com.dre.brewery.storage.records.ConfiguredRedisManager;
import com.github.Anon8281.universalScheduler.UniversalRunnable;

public class NormalShardedRedis extends AbstractRedisPubSub {

    private static String masterId;

    public NormalShardedRedis(ConfiguredRedisManager record) throws RedisInitException {
        super(record);
        startHandshakeRequestTask();
    }

    private void startHandshakeRequestTask() {
        new UniversalRunnable() {
            @Override
            public void run() {
                if (masterId != null) {
                    this.cancel();
                    plugin.debugLog("Master ID found, stopping handshake request task");
                    return;
                }

                redisLog("Publishing handshake request. Waiting for a master shard to respond...");
                publish(RedisMessage.HANDSHAKE_REQUEST, null);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 100);
    }

    @Override
    public void handshakeRequest(String normalShardId) {
        // Nothing to do here
    }

    @Override
    public void handshakeResponse(String newId) {
        if (masterId != null) {
            redisLog("Got a handshake response from: &6" + newId + " &abut already have a master ID: &6" + masterId + "&a. Replacing it.");
        }
        masterId = newId;
        redisLog("Got a handshake response from: &6" + newId);
    }


    @Override
    public void pushCache() {
        genericPushCache();
        publish(RedisMessage.FINISHED_TASK, masterId);
    }

    @Override
    public void retrieveCache() {
        genericRetrieveCache();
        //publish(RedisMessage.FINISHED_TASK, masterId);
    }

    @Override
    public void save() {
        redisLog("Ignoring save request because this is a normal Redis shard");
    }

    @Override
    public void finishedTask(String from) {
        // Ignore
    }

    @Override
    public void masterShutdown() {
        redisLog("Master shard has disconnected! Starting handshake request task...");
        masterId = null;
        startHandshakeRequestTask();
    }

    public void sendSaveRequest() {
        publish(RedisMessage.SAVE, masterId);
    }
}
