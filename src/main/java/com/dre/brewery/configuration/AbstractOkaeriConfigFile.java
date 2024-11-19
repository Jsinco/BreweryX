package com.dre.brewery.configuration;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.configuration.serdes.MaterialTransformer;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.exception.OkaeriException;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class AbstractOkaeriConfigFile extends OkaeriConfig {

    protected static final Path DATA_FOLDER = BreweryPlugin.getInstance().getDataFolder().toPath();


    protected static final List<Supplier<BidirectionalTransformer<?, ?>>> DEFAULT_TRANSFORMERS = List.of(
        MaterialTransformer::new
    );


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

            for (Supplier<BidirectionalTransformer<?, ?>> packSupplier : DEFAULT_TRANSFORMERS) {
                it.withSerdesPack(registry -> registry.register(packSupplier.get()));
            }
            it.load(true);
        });
    }


    // Util methods

    public void saveAsync() throws OkaeriException {
        CompletableFuture.runAsync(this::save);
    }
}
