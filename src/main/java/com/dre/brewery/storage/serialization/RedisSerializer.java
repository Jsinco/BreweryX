package com.dre.brewery.storage.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public class RedisSerializer {

    private final Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();

    public String serialize(Object object) {
        return gson.toJson(object);
    }

    public <T> T deserialize(String data, Class<T> type) {
        if (data == null) {
            return null;
        }
        return gson.fromJson(data, type);
    }
}
