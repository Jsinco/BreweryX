package com.dre.brewery.files;

import com.dre.brewery.BreweryPlugin;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractOkaeriConfigFile extends OkaeriConfig {

    protected static final Path DATA_FOLDER = BreweryPlugin.getInstance().getDataFolder().toPath();


    public static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, BidirectionalTransformer<?, ?>... transformers) {
        return createConfig(configClass, configClass.getSimpleName().toLowerCase() + ".yml", new YamlSnakeYamlConfigurer(), transformers);
    }

    public static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, String fileName, BidirectionalTransformer<?, ?>... transformers) {
        return createConfig(configClass, fileName, new YamlSnakeYamlConfigurer(), new StandardSerdes(), transformers);
    }

    public static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, String fileName, Configurer configurer, BidirectionalTransformer<?, ?>... transformers) {
        return createConfig(configClass, fileName, configurer, new StandardSerdes(), transformers);
    }


    public static <T extends AbstractOkaeriConfigFile> T createConfig(Class<T> configClass, String fileName, Configurer configurer, OkaeriSerdesPack serdesPack, BidirectionalTransformer<?, ?>... transformers) {
        return ConfigManager.create(configClass, (it) -> {
            it.withConfigurer(configurer, serdesPack);
            it.withBindFile(DATA_FOLDER.resolve(fileName));
            it.withRemoveOrphans(false); // Just leaving this off for now
            it.saveDefaults();

            for (BidirectionalTransformer<?, ?> pack : transformers) {
                it.withSerdesPack(registry -> registry.register(pack));
            }

            it.load(true);
        });
    }


    // Util methods

    public void saveAsync() throws OkaeriException {
        CompletableFuture.runAsync(this::save);
    }
}
