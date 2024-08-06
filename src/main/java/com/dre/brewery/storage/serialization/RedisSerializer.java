package com.dre.brewery.storage.serialization;

import com.dre.brewery.storage.records.SerializableThing;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class RedisSerializer extends SQLDataSerializer {

    @Override
    public String serialize(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T deserialize(String data, Class<T> type) {
        return gson.fromJson(data, type);
    }


    public <T> T deserialize(String data, Type type) {
        return gson.fromJson(data, type);
    }


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
