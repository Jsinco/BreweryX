package com.dre.brewery.files;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.files.serdes.MaterialTransformer;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractOkaeriConfigFile extends OkaeriConfig {

    protected static final Path DATA_FOLDER = BreweryPlugin.getInstance().getDataFolder().toPath();


    public static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass) {
        return createConfig(configClass, configClass.getSimpleName().toLowerCase() + ".yml", new YamlSnakeYamlConfigurer());
    }

    public static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, String fileName) {
        return createConfig(configClass, fileName, new YamlSnakeYamlConfigurer(), new StandardSerdes());
    }

    public static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, String fileName, Configurer configurer) {
        return createConfig(configClass, fileName, configurer, new StandardSerdes());
    }

    public static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, String fileName, Configurer configurer, OkaeriSerdesPack serdesPack) {
        return ConfigManager.create(configClass, (it) -> {
            it.withConfigurer(configurer, serdesPack);
            it.withBindFile(DATA_FOLDER.resolve(fileName));
            it.withRemoveOrphans(false); // Just leaving this off for now
            it.saveDefaults();

			// why was this in a function parameter? no need to define different transformers for each config
			it.withSerdesPack(registry -> registry.register(new MaterialTransformer()));

            it.load(true);
        });
    }


    // Util methods

    public void saveAsync() throws OkaeriException {
        CompletableFuture.runAsync(this::save);
    }
}
