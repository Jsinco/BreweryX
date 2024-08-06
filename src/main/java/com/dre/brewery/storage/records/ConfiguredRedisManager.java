package com.dre.brewery.storage.records;

import com.dre.brewery.storage.RedisInitException;
import com.dre.brewery.storage.redis.RedisFamilyType;

public record ConfiguredRedisManager(boolean enabled, String address, String password, RedisFamilyType type, String id) {

    public String host() {
        if (address == null) {
            return null;
        } else if (!address.contains(":")) {
            return address;
        }
        return address.split(":")[0];
    }

    public int port() {
        if (address == null) {
            return 6379;
        } else if (!address.contains(":")) {
            return 6379;
        }
        return Integer.parseInt(address.split(":")[1]);
    }

    public String getIdSafely() throws RedisInitException {
        if (id != null && id.isEmpty()) {
            throw new RedisInitException("ID cannot be empty. This is a configuration error. Set an ID in your config.yml at: &9redis.id");
        } else {
            return id;
        }
    }
}
