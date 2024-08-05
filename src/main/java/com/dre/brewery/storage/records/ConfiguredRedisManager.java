package com.dre.brewery.storage.records;

import com.dre.brewery.storage.redis.RedisFamilyType;

public record ConfiguredRedisManager(boolean enabled, String address, String username, String password, boolean ssl, RedisFamilyType type) {
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
}
