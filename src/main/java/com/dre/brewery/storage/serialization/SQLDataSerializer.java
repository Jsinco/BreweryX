package com.dre.brewery.storage.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;
import java.util.Base64;

/**
 * Serializes the given object to JSON and then to a Base64 encoded string.
 */
public class SQLDataSerializer {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.STATIC).create();

    public String serialize(Object object) {
        return Base64.getEncoder().encodeToString(gson.toJson(object).getBytes());
    }

    public <T> T deserialize(String data, Class<T> type) {
        return gson.fromJson(new String(Base64.getDecoder().decode(data)), type);
    }

    public <T> T deserialize(String data, Class<T> type, T defaultValue) {
        try {
            return deserialize(data, type);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
