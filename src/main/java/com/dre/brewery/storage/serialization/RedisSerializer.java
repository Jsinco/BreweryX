package com.dre.brewery.storage.serialization;

import com.dre.brewery.storage.records.SerializableThing;

import java.util.List;

public class RedisSerializer extends SQLDataSerializer {

    public String[] serializeArray(List<? extends SerializableThing> serializables) {
        if (serializables == null || serializables.isEmpty()) {
            return null;
        }
        return serializables.stream()
                .map(this::serialize)
                .toArray(String[]::new);
    }

    public <T extends SerializableThing> List<T> deserializeArray(List<String> serialized, Class<T> clazz) {
        return serialized.stream()
                .map(s -> this.deserialize(s, clazz))
                .toList();
    }
}
