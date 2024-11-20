package com.dre.brewery.configuration;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.configurer.BreweryXConfigurer;
import com.dre.brewery.configuration.configurer.TranslationManager;
import com.dre.brewery.configuration.serdes.MaterialTransformer;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigManager {

    @Getter
    private static final Map<Class<? extends AbstractOkaeriConfigFile>, AbstractOkaeriConfigFile> LOADED_CONFIGS = new HashMap<>();

    private static final Path DATA_FOLDER = BreweryPlugin.getInstance().getDataFolder().toPath();
    private static final Map<Class<? extends Configurer>, Supplier<Configurer>> CONFIGURERS = Map.of(
            BreweryXConfigurer.class, BreweryXConfigurer::new,
            YamlSnakeYamlConfigurer.class, YamlSnakeYamlConfigurer::new
    );
    protected static final List<Supplier<BidirectionalTransformer<?, ?>>> TRANSFORMERS = List.of(
            MaterialTransformer::new
    );


    /**
     * Get a config instance from the LOADED_CONFIGS map, or create a new instance if it doesn't exist
     * @param configClass The class of the config to get
     * @return The config instance
     * @param <T> The type of the config
     */
    public static <T extends AbstractOkaeriConfigFile> T getConfig(Class<T> configClass) {
        for (var mapEntry : LOADED_CONFIGS.entrySet()) {
            if (mapEntry.getKey().equals(configClass)) {
                return (T) mapEntry.getValue();
            }
        }
        return createConfig(configClass);
    }

    /**
     * Replaces a config instance in the LOADED_CONFIGS map with a new instance of the same class
     * @param configClass The class of the config to replace
     * @param <T> The type of the config
     */
    public static <T extends AbstractOkaeriConfigFile> void newInstance(T configClass) {
        LOADED_CONFIGS.put(configClass.getClass(),
                createConfig(configClass.getClass(),
                        configClass.getBindFile().getFileName().toString(),
                        configClass.getConfigurer(),
                        new StandardSerdes(),
                        getOkaeriConfigFileOptions(configClass.getClass()).update()));
    }


    /**
     * Get the file name of a config class
     * @param configClass The class of the config to get the file name of
     * @return The file name
     * @param <T> The type of the config
     */
    public static <T extends AbstractOkaeriConfigFile> String getFileName(Class<T> configClass) {
        OkaeriConfigFileOptions options = getOkaeriConfigFileOptions(configClass);

        if (!options.useLangFileName()) {
            return options.value();
        } else {
            return "langs/" + TranslationManager.getInstance().getActiveTranslation().getFilename();
        }
    }



    /**
     * Create a new config instance with a custom file name, configurer, serdes pack, and puts it in the LOADED_CONFIGS map
     * @param configClass The class of the config to create
     * @param fileName The name of the file to bind the config to
     * @param configurer The configurer to use
     * @param serdesPack The serdes pack to use
     * @return The new config instance
     * @param <T> The type of the config
     */
    private static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, String fileName, Configurer configurer, OkaeriSerdesPack serdesPack, boolean update) {
        T instance = eu.okaeri.configs.ConfigManager.create(configClass, (it) -> {
            it.withConfigurer(configurer, serdesPack);
            it.withBindFile(DATA_FOLDER.resolve(fileName));
            it.withRemoveOrphans(false); // Just leaving this off for now
            it.saveDefaults();

            for (Supplier<BidirectionalTransformer<?, ?>> packSupplier : TRANSFORMERS) {
                it.withSerdesPack(registry -> registry.register(packSupplier.get()));
            }
            it.load(update);
        });
        LOADED_CONFIGS.put(configClass ,instance);
        return instance;
    }

    /**
     * Create a new config instance using a config class' annotation
     * @param configClass The class of the config to create
     * @return The new config instance
     * @param <T> The type of the config
     */
    private static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass) {
        OkaeriConfigFileOptions options = configClass.getAnnotation(OkaeriConfigFileOptions.class);

        return createConfig(configClass, getFileName(configClass), CONFIGURERS.get(options.configurer()).get(), new StandardSerdes(), options.update());
    }


    // Util

    public static void createFileFromResources(String resourcesPath, Path destination) {
        Path targetDir = destination.getParent();

        try {
            // Ensure the directory exists, create it if necessary
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }


            if (!Files.exists(destination)) {
                try (InputStream inputStream = BreweryPlugin.class.getClassLoader().getResourceAsStream(resourcesPath)) {

                    if (inputStream != null) {
                        // Copy the input stream content to the target file
                        Files.copy(inputStream, destination);
                    } else {
                        BreweryPlugin.getInstance().warningLog("Could not find resource file for " + resourcesPath);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating or copying file", e);
        }
    }


    private static OkaeriConfigFileOptions getOkaeriConfigFileOptions(Class<? extends AbstractOkaeriConfigFile> configClass) {
        OkaeriConfigFileOptions options = configClass.getAnnotation(OkaeriConfigFileOptions.class);
        if (options == null) {
            options = new OkaeriConfigFileOptions() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return OkaeriConfigFileOptions.class;
                }

                @Override
                public Class<? extends Configurer> configurer() {
                    return BreweryXConfigurer.class;
                }

                @Override
                public boolean useLangFileName() {
                    return false;
                }

                @Override
                public boolean update() {
                    return false;
                }

                @Override
                public String value() {
                    return configClass.getSimpleName().toLowerCase() + ".yml";
                }
            };
        }
        return options;
    }
}
