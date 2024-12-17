/*
 * BreweryX Bukkit-Plugin for an alternate brewing process
 * Copyright (C) 2024 The Brewery Team
 *
 * This file is part of BreweryX.
 *
 * BreweryX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BreweryX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BreweryX. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package com.dre.brewery.configuration;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.configurer.BreweryXConfigurer;
import com.dre.brewery.configuration.configurer.TranslationManager;
import com.dre.brewery.utility.Logging;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A class which manages the creation and retrieval of config files. This class
 * can be used as a singleton {@link ConfigManager} or as a standalone class {@link com.dre.brewery.api.addons.AddonConfigManager}.
 */
public class ConfigHead {

    public final Map<Class<? extends Configurer>, Configurer> CONFIGURERS = new HashMap<>(Map.of(
            BreweryXConfigurer.class, new BreweryXConfigurer(),
            YamlSnakeYamlConfigurer.class, new YamlSnakeYamlConfigurer()
    ));

    public final Map<Class<? extends AbstractOkaeriConfigFile>, AbstractOkaeriConfigFile> LOADED_CONFIGS = new HashMap<>();
    public Path DATA_FOLDER = BreweryPlugin.getInstance().getDataFolder().toPath();


    public ConfigHead() {
    }

    public ConfigHead(Path dataFolder) {
        this.DATA_FOLDER = dataFolder;
    }


    /**
     * Get a config instance from the LOADED_CONFIGS map, or create a new instance if it doesn't exist
     * @param configClass The class of the config to get
     * @return The config instance
     * @param <T> The type of the config
     */
    public <T extends AbstractOkaeriConfigFile> T getConfig(Class<T> configClass) {
        try {
            for (var mapEntry : LOADED_CONFIGS.entrySet()) {
                if (mapEntry.getKey().equals(configClass)) {
                    return (T) mapEntry.getValue();
                }
            }
            return createConfig(configClass);
        } catch (Throwable e) {
            Logging.errorLog("Something went wrong trying to load a config file! &e(" + configClass.getSimpleName() + ".yml)", e);
            Logging.warningLog("Resolve the issue in the file and run &6/brewery reload");
            return createBlankConfigInstance(configClass);
        }
    }

    /**
     * Replaces a config instance in the LOADED_CONFIGS map with a new instance of the same class
     * @param configClass The class of the config to replace
     * @param <T> The type of the config
     */
    public <T extends AbstractOkaeriConfigFile> void newInstance(Class<T> configClass, boolean overwrite) {
        if (!overwrite && LOADED_CONFIGS.containsKey(configClass)) {
            return;
        }
        LOADED_CONFIGS.put(configClass, createConfig(configClass));
    }


    /**
     * Get the file path of a config class
     * @param configClass The class of the config to get the file name of
     * @return The file name
     * @param <T> The type of the config
     */
    public <T extends AbstractOkaeriConfigFile> Path getFilePath(Class<T> configClass) {
        OkaeriConfigFileOptions options = getOkaeriConfigFileOptions(configClass);

        if (!options.useLangFileName()) {
            return DATA_FOLDER.resolve(options.value());
        } else {
            return DATA_FOLDER.resolve("languages/" + TranslationManager.getInstance().getActiveTranslation().getFilename());
        }
    }




    /**
     * Create a new config instance with a custom file name, configurer, serdes pack, and puts it in the LOADED_CONFIGS map
     * @param configClass The class of the config to create
     * @param file The file to use
     * @param configurer The configurer to use
     * @param serdesPack The serdes pack to use
     * @return The new config instance
     * @param <T> The type of the config
     */
    public <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, Path file, Configurer configurer, OkaeriSerdesPack serdesPack, boolean update, boolean removeOrphans) {
        boolean firstCreation = !Files.exists(file);

        T instance = eu.okaeri.configs.ConfigManager.create(configClass, (it) -> {
            it.withConfigurer(configurer, serdesPack);
            it.withRemoveOrphans(removeOrphans);
            it.withBindFile(file);
            it.saveDefaults();
            it.load(update);
        });

        instance.setUpdate(update);
        instance.setFirstCreation(firstCreation);
        LOADED_CONFIGS.put(configClass, instance);
        return instance;
    }

    /**
     * Create a new config instance using a config class' annotation
     * @param configClass The class of the config to create
     * @return The new config instance
     * @param <T> The type of the config
     */
    public <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass) {
        OkaeriConfigFileOptions options = getOkaeriConfigFileOptions(configClass);

        Configurer configurer = CONFIGURERS.get(options.configurer());
        if (configurer == null) {
            Logging.errorLog("Configurer cannot be null. Make sure you've registered the configurer before trying to use it!");
            configurer = CONFIGURERS.get(BreweryXConfigurer.class);
        }

        return createConfig(configClass, getFilePath(configClass), configurer, new StandardSerdes(), options.update(), options.removeOrphans());
    }

    @Nullable
    public <T extends AbstractOkaeriConfigFile> T createBlankConfigInstance(Class<T> configClass) {
        try {
            T inst = configClass.getDeclaredConstructor().newInstance();
            inst.setBlankInstance(true);
            LOADED_CONFIGS.put(configClass, inst);
            return inst;
        } catch (Exception e) {
            Logging.errorLog("Failed to create a blank config instance for " + configClass.getSimpleName(), e);
            return null;
        }
    }


    /**
     * Adds the provided OkaeriSerdesPack instances to all registered configurers.
     * Each serdes pack is registered to every configurer in the CONFIGURERS map.
     *
     * @param packs The array of OkaeriSerdesPack instances to be added to the configurers.
     */
    public void addSerdesPacks(OkaeriSerdesPack... packs) {
        CONFIGURERS.values().forEach(configurer -> {
            for (OkaeriSerdesPack pack : packs) {
                configurer.register(pack);
            }
        });
    }

    /**
     * Adds the provided BidirectionalTransformer instances to all registered configurers.
     * Each transformer is registered to every configurer in the CONFIGURERS map.
     *
     * @param transformers The array of BidirectionalTransformer instances to be added to the configurers.
     */
    public void addBidirectionalTransformers(BidirectionalTransformer<?, ?>... transformers) {
        CONFIGURERS.values().forEach(configurer ->  {
            for (BidirectionalTransformer<?, ?> transformer : transformers) {
                configurer.register(registry -> registry.register(transformer));
            }
        });
    }

    /**
     * Adds the provided configurer instance to the CONFIGURERS map.
     * The configurer is stored with its class as the key.
     *
     * @param configurer The Configurer instance to be added.
     */
    public void addConfigurer(Configurer configurer) {
        CONFIGURERS.put(configurer.getClass(), configurer);
    }

    // Util

    public void createFileFromResources(String resourcesPath, Path destination) {
        Path targetDir = destination.getParent();

        try {
            // Ensure the directory exists, create it if necessary
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            if (Files.exists(destination)) {
                return;
            }

            try (InputStream inputStream = BreweryPlugin.class.getClassLoader().getResourceAsStream(resourcesPath)) {

                if (inputStream != null) {
                    // Copy the input stream content to the target file
                    Files.copy(inputStream, destination);
                } else {
                    Logging.warningLog("Could not find resource file for " + resourcesPath);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating or copying file", e);
        }
    }


    public OkaeriConfigFileOptions getOkaeriConfigFileOptions(Class<? extends AbstractOkaeriConfigFile> configClass) {
        OkaeriConfigFileOptions options = configClass.getAnnotation(OkaeriConfigFileOptions.class);
        if (options == null) {
            options = new OkaeriConfigFileOptions() {
                @Override public Class<? extends Annotation> annotationType() { return OkaeriConfigFileOptions.class; }
                @Override public Class<? extends Configurer> configurer() { return BreweryXConfigurer.class; }
                @Override public boolean useLangFileName() { return false; }
                @Override public boolean update() { return false; }
                @Override public boolean removeOrphans() { return false; }
                @Override public String value() { return configClass.getSimpleName().toLowerCase() + ".yml"; }
            };
        }
        return options;
    }
}
