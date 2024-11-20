package com.dre.brewery.configuration.sector;

import com.dre.brewery.BreweryPlugin;
import eu.okaeri.configs.OkaeriConfig;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract class to aid in the creation of OkaeriConfig sectors
 * @param <T> The type of the capsule
 */
public abstract class AbstractOkaeriConfigSector<T> extends OkaeriConfig {

    public Map<String, T> getCapsules() {
        Map<String, T> map = new HashMap<>();

        // Get the actual class of the generic type T
        Class<?> typeOfT = getTypeOfT();

        for (Field field : this.getClass().getDeclaredFields()) {
            // Check if the field's type matches T
            if (Objects.equals(field.getType(), typeOfT)) {
                try {
                    T obj = (T) field.get(this);
                    map.put(field.getName(), obj);
                } catch (IllegalAccessException e) {
                    BreweryPlugin.getInstance().errorLog("Failed to access field: " + field.getName(), e);
                }
            }
        }
        return map;
    }

    // Helper method to get the type of T
    private Class<?> getTypeOfT() {
        // Check the generic type of the superclass
        // For example, OkaeriConfigSector<ConfigRecipe> would give us String as the type
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }
}
